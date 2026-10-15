package com.igorwojda.jellymobile.feature.favorites.data.repository

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSourceHealthTracker
import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteMovieDao
import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteMovieEntity
import com.igorwojda.jellymobile.feature.favorites.data.database.SyncState
import com.igorwojda.jellymobile.feature.favorites.data.datasource.remote.FavoriteRemoteDataSource
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class FavoriteRepositoryImplTest {
    private val dao = FakeFavoriteMovieDao()
    private val remote = FakeFavoriteRemoteDataSource()

    @Test
    fun `adds a favorite while the backend is unavailable and keeps it pending`() =
        runRepositoryTest { repository ->
            remote.failure = IOException("Connection refused")

            repository.toggle(dune)

            assertEquals(listOf(dune), repository.favorites.value)
            assertEquals(SyncState.PENDING_ADD, dao.syncStateOf(dune.id))
        }

    @Test
    fun `sends pending changes once the backend is back`() =
        runRepositoryTest { repository ->
            remote.failure = IOException("Connection refused")
            repository.toggle(dune)
            remote.failure = null
            remote.favorites = listOf(alien)

            repository.refresh(force = true)

            assertEquals(listOf(dune.id), remote.addedIds)
            assertEquals(SyncState.SYNCED, dao.syncStateOf(dune.id))
            // Pulled from the backend, which has the pushed movie first.
            assertEquals(listOf(dune.id, alien.id), repository.favorites.value.map { it.id })
        }

    @Test
    fun `hides a favorite removed offline and removes it from the backend later`() =
        runRepositoryTest { repository ->
            remote.favorites = listOf(dune)
            repository.refresh(force = true)
            remote.failure = IOException("Connection refused")

            repository.toggle(dune)

            assertEquals(emptyList(), repository.favorites.value)
            assertEquals(SyncState.PENDING_REMOVE, dao.syncStateOf(dune.id))

            remote.failure = null
            repository.refresh(force = true)

            assertEquals(listOf(dune.id), remote.removedIds)
            assertNull(dao.syncStateOf(dune.id))
        }

    @Test
    fun `keeps pending changes when the backend list doesn't have them yet`() =
        runRepositoryTest { repository ->
            remote.failure = IOException("Connection refused")
            repository.toggle(dune)

            dao.replaceSynced(listOf(alien.toSyncedEntity()))

            assertEquals(SyncState.PENDING_ADD, dao.syncStateOf(dune.id))
            assertEquals(setOf(dune, alien), repository.favorites.value.toSet())
        }

    @Test
    fun `reverts and reports a change the backend rejects`() =
        runRepositoryTest { repository ->
            remote.failure = clientError(HttpStatusCode.NotFound)

            assertFailsWith<DataException.NotFound> { repository.toggle(dune) }

            assertEquals(emptyList(), repository.favorites.value)
            assertNull(dao.syncStateOf(dune.id))
        }

    @Test
    fun `refresh without force downloads the favorites only once`() =
        runRepositoryTest { repository ->
            remote.favorites = listOf(dune)
            repository.refresh(force = false)
            remote.favorites = listOf(alien)

            repository.refresh(force = false)

            assertEquals(listOf(dune), repository.favorites.value)
        }

    // Unconfined: the favorites StateFlow sees each database change before the assertions run.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun runRepositoryTest(block: suspend TestScope.(FavoriteRepositoryImpl) -> Unit) =
        runTest(UnconfinedTestDispatcher()) {
            val repository =
                FavoriteRepositoryImpl(
                    remoteDataSources = RemoteDataSourceChain(listOf(remote), RemoteSourceHealthTracker()),
                    favoriteMovieDao = dao,
                    applicationScope = backgroundScope,
                    clock = IncrementingClock(),
                )
            block(repository)
        }

    private suspend fun clientError(status: HttpStatusCode): Exception {
        val httpClient = HttpClient(MockEngine { respondError(status) }) { expectSuccess = true }
        return assertFailsWith<ClientRequestException> { httpClient.get("https://example.com") }
    }

    private fun FavoriteMovie.toSyncedEntity() =
        FavoriteMovieEntity(id, title, posterUrl, addedAtMillis = 0, syncState = SyncState.SYNCED)

    private companion object {
        val dune = FavoriteMovie(id = 1, title = "Dune", posterUrl = null)
        val alien = FavoriteMovie(id = 2, title = "Alien", posterUrl = null)
    }
}

/** Every call is a millisecond later, so favorites have distinct, ordered timestamps. */
private class IncrementingClock : Clock {
    private var millis = 1_000_000L

    override fun now(): Instant = Instant.fromEpochMilliseconds(millis++)
}

/** Backend keeping favorites like the real one: added movies show up in [getFavorites]. */
private class FakeFavoriteRemoteDataSource : FavoriteRemoteDataSource {
    override val source = RemoteSource.BACKEND
    var failure: Exception? = null
    var favorites = emptyList<FavoriteMovie>()
    val addedIds = mutableListOf<Long>()
    val removedIds = mutableListOf<Long>()

    override suspend fun getFavorites(): List<FavoriteMovie> {
        failure?.let { throw it }
        return favorites
    }

    override suspend fun add(movieId: Long) {
        failure?.let { throw it }
        addedIds += movieId
        favorites = listOf(FavoriteMovie(movieId, title = "Movie $movieId", posterUrl = null)) + favorites
    }

    override suspend fun remove(movieId: Long) {
        failure?.let { throw it }
        removedIds += movieId
        favorites = favorites.filterNot { it.id == movieId }
    }
}

/** In-memory [FavoriteMovieDao] with the same queries; the `@Transaction` functions run the interface's defaults. */
private class FakeFavoriteMovieDao : FavoriteMovieDao {
    private val rows = MutableStateFlow<Map<Long, FavoriteMovieEntity>>(emptyMap())

    fun syncStateOf(movieId: Long) = rows.value[movieId]?.syncState

    override fun observeFavorites(): Flow<List<FavoriteMovieEntity>> =
        rows.map { favorites ->
            favorites.values.filter { it.syncState != SyncState.PENDING_REMOVE }.sortedByDescending { it.addedAtMillis }
        }

    override suspend fun getPendingChanges() =
        rows.value.values.filter { it.syncState != SyncState.SYNCED }.sortedBy { it.addedAtMillis }

    override suspend fun getFavorite(movieId: Long) = rows.value[movieId]

    override suspend fun upsert(favorite: FavoriteMovieEntity) = rows.update { it + (favorite.id to favorite) }

    override suspend fun markPendingRemove(movieId: Long) = updateIf(movieId, from = null, to = SyncState.PENDING_REMOVE)

    override suspend fun confirmAdd(movieId: Long) = updateIf(movieId, from = SyncState.PENDING_ADD, to = SyncState.SYNCED)

    override suspend fun confirmRemove(movieId: Long) = deleteIf(movieId, SyncState.PENDING_REMOVE)

    override suspend fun revertAdd(movieId: Long) = deleteIf(movieId, SyncState.PENDING_ADD)

    override suspend fun revertRemove(movieId: Long) = updateIf(movieId, from = SyncState.PENDING_REMOVE, to = SyncState.SYNCED)

    override suspend fun deleteSynced() = rows.update { favorites -> favorites.filterValues { it.syncState != SyncState.SYNCED } }

    override suspend fun insertIgnoringPending(favorites: List<FavoriteMovieEntity>) =
        rows.update { existing -> favorites.associateBy { it.id } + existing }

    private fun updateIf(
        movieId: Long,
        from: SyncState?,
        to: SyncState,
    ) = rows.update { favorites ->
        val favorite = favorites[movieId]
        if (favorite == null || (from != null && favorite.syncState != from)) {
            favorites
        } else {
            favorites + (movieId to favorite.copy(syncState = to))
        }
    }

    private fun deleteIf(
        movieId: Long,
        syncState: SyncState,
    ) = rows.update { favorites -> if (favorites[movieId]?.syncState == syncState) favorites - movieId else favorites }
}
