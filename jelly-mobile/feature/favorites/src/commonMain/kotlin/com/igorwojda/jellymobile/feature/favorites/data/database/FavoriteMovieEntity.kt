package com.igorwojda.jellymobile.feature.favorites.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie

/** Favorite saved on the device. Changes the backend hasn't confirmed yet are marked by [syncState]. */
@Entity(tableName = "favorite_movie")
internal data class FavoriteMovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val posterUrl: String?,
    /** Sort key, most recent first. */
    val addedAtMillis: Long,
    val syncState: SyncState,
)

internal enum class SyncState {
    /** Matches the backend. */
    SYNCED,

    /** Added on the device, not yet sent to the backend. */
    PENDING_ADD,

    /** Removed on the device, not yet sent to the backend. Hidden from the user. */
    PENDING_REMOVE,
}

internal fun FavoriteMovieEntity.toDomain() = FavoriteMovie(id = id, title = title, posterUrl = posterUrl)

internal fun FavoriteMovie.toEntity(
    addedAtMillis: Long,
    syncState: SyncState,
) = FavoriteMovieEntity(id = id, title = title, posterUrl = posterUrl, addedAtMillis = addedAtMillis, syncState = syncState)
