package com.igorwojda.jellymobile.feature.favorites.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

// The SyncState conditions on the updates make them no-ops when the user changed the favorite again in the meantime,
// so a late backend confirmation never overwrites a newer change.
@Dao
internal interface FavoriteMovieDao {
    /** Favorites as the user sees them: synced and pending additions, most recent first. */
    @Query("SELECT * FROM favorite_movie WHERE syncState != 'PENDING_REMOVE' ORDER BY addedAtMillis DESC")
    fun observeFavorites(): Flow<List<FavoriteMovieEntity>>

    /** Changes not yet sent to the backend, oldest first. */
    @Query("SELECT * FROM favorite_movie WHERE syncState != 'SYNCED' ORDER BY addedAtMillis")
    suspend fun getPendingChanges(): List<FavoriteMovieEntity>

    /** Adds [favorite] (a pending addition), or marks it as removed when it's already a favorite. */
    @Transaction
    suspend fun toggle(favorite: FavoriteMovieEntity) {
        val existing = getFavorite(favorite.id)
        if (existing != null && existing.syncState != SyncState.PENDING_REMOVE) {
            markPendingRemove(favorite.id)
        } else {
            upsert(favorite)
        }
    }

    @Query("SELECT * FROM favorite_movie WHERE id = :movieId")
    suspend fun getFavorite(movieId: Long): FavoriteMovieEntity?

    @Upsert
    suspend fun upsert(favorite: FavoriteMovieEntity)

    @Query("UPDATE favorite_movie SET syncState = 'PENDING_REMOVE' WHERE id = :movieId")
    suspend fun markPendingRemove(movieId: Long)

    /** The backend saved the addition. */
    @Query("UPDATE favorite_movie SET syncState = 'SYNCED' WHERE id = :movieId AND syncState = 'PENDING_ADD'")
    suspend fun confirmAdd(movieId: Long)

    /** The backend saved the removal. */
    @Query("DELETE FROM favorite_movie WHERE id = :movieId AND syncState = 'PENDING_REMOVE'")
    suspend fun confirmRemove(movieId: Long)

    /** The backend rejected the addition. */
    @Query("DELETE FROM favorite_movie WHERE id = :movieId AND syncState = 'PENDING_ADD'")
    suspend fun revertAdd(movieId: Long)

    /** The backend rejected the removal. */
    @Query("UPDATE favorite_movie SET syncState = 'SYNCED' WHERE id = :movieId AND syncState = 'PENDING_REMOVE'")
    suspend fun revertRemove(movieId: Long)

    /** Replaces the synced favorites with the backend's [favorites], keeping changes not yet sent to the backend. */
    @Transaction
    suspend fun replaceSynced(favorites: List<FavoriteMovieEntity>) {
        deleteSynced()
        insertIgnoringPending(favorites)
    }

    @Query("DELETE FROM favorite_movie WHERE syncState = 'SYNCED'")
    suspend fun deleteSynced()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringPending(favorites: List<FavoriteMovieEntity>)
}
