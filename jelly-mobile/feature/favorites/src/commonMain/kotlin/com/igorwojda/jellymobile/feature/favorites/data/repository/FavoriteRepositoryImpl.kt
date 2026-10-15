package com.igorwojda.jellymobile.feature.favorites.data.repository

import com.igorwojda.jellymobile.feature.base.coroutines.suspendRunCatching
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.data.remote.isSourceUnavailable
import com.igorwojda.jellymobile.feature.base.data.remote.mapDataErrors
import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteMovieDao
import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteMovieEntity
import com.igorwojda.jellymobile.feature.favorites.data.database.SyncState
import com.igorwojda.jellymobile.feature.favorites.data.database.toDomain
import com.igorwojda.jellymobile.feature.favorites.data.database.toEntity
import com.igorwojda.jellymobile.feature.favorites.data.datasource.remote.FavoriteRemoteDataSource
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FavoriteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

/**
 * Favorites saved on the device (Room), the source of truth every screen observes, synced with the backend.
 *
 * Changes are saved locally first and marked pending until the backend confirms them, so favorites keep working
 * offline. Pending changes are sent right away when possible, otherwise with the next [refresh]. Failures are thrown
 * as `DataException`.
 */
internal class FavoriteRepositoryImpl(
    private val remoteDataSources: RemoteDataSourceChain<FavoriteRemoteDataSource>,
    private val favoriteMovieDao: FavoriteMovieDao,
    applicationScope: CoroutineScope,
    private val clock: Clock = Clock.System,
) : FavoriteRepository {
    override val favorites: StateFlow<List<FavoriteMovie>> =
        favoriteMovieDao
            .observeFavorites()
            .map { favorites -> favorites.map { it.toDomain() } }
            .stateIn(applicationScope, SharingStarted.Eagerly, initialValue = emptyList())

    // One sync at a time, so changes reach the backend in the order the user made them.
    private val syncMutex = Mutex()
    private var isLoaded = false

    override suspend fun refresh(force: Boolean): Unit =
        mapDataErrors {
            syncMutex.withLock {
                if (isLoaded && !force) return@withLock

                // First, so the backend's list already contains the changes made on the device. Rejected changes are
                // reverted; there is no one to report them to during a background refresh.
                pushPendingChanges()

                val now = clock.now().toEpochMilliseconds()
                val remoteFavorites = remoteDataSources.request { it.getFavorites() }
                favoriteMovieDao.replaceSynced(
                    // The backend sends the most recent first, without dates: descending timestamps keep its order.
                    remoteFavorites.mapIndexed { index, movie ->
                        movie.toEntity(addedAtMillis = now - index, syncState = SyncState.SYNCED)
                    },
                )
                isLoaded = true
            }
        }

    override suspend fun toggle(movie: FavoriteMovie): Unit =
        mapDataErrors {
            favoriteMovieDao.toggle(
                movie.toEntity(addedAtMillis = clock.now().toEpochMilliseconds(), syncState = SyncState.PENDING_ADD),
            )

            val rejection =
                suspendRunCatching { syncMutex.withLock { pushPendingChanges() } }
                    // Backend unavailable: the change is saved on the device and sent with the next refresh.
                    .getOrNull()
            rejection?.let { throw it }
        }

    /**
     * Sends the pending changes to the backend, oldest first. Changes the backend rejects (4xx) are reverted and the
     * first rejection is returned. Throws when the backend is unavailable; the remaining changes stay pending.
     */
    private suspend fun pushPendingChanges(): Exception? {
        var rejection: Exception? = null

        for (change in favoriteMovieDao.getPendingChanges()) {
            try {
                send(change)
            } catch (e: CancellationException) {
                throw e
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                if (e.isSourceUnavailable()) throw e
                revert(change)
                rejection = rejection ?: e
            }
        }
        return rejection
    }

    private suspend fun send(change: FavoriteMovieEntity) {
        when (change.syncState) {
            SyncState.PENDING_ADD -> {
                remoteDataSources.request { it.add(change.id) }
                favoriteMovieDao.confirmAdd(change.id)
            }
            SyncState.PENDING_REMOVE -> {
                remoteDataSources.request { it.remove(change.id) }
                favoriteMovieDao.confirmRemove(change.id)
            }
            SyncState.SYNCED -> Unit
        }
    }

    private suspend fun revert(change: FavoriteMovieEntity) {
        when (change.syncState) {
            SyncState.PENDING_ADD -> favoriteMovieDao.revertAdd(change.id)
            SyncState.PENDING_REMOVE -> favoriteMovieDao.revertRemove(change.id)
            SyncState.SYNCED -> Unit
        }
    }
}
