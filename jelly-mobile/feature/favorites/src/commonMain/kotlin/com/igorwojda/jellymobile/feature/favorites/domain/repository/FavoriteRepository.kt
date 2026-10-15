package com.igorwojda.jellymobile.feature.favorites.domain.repository

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import kotlinx.coroutines.flow.StateFlow

/** The user's favorite movies, saved on the device and synced with the backend. */
interface FavoriteRepository {
    /** Favorites saved on the device, most recently added first. Available offline. */
    val favorites: StateFlow<List<FavoriteMovie>>

    /**
     * Sends the changes made on the device to the backend and downloads the backend's favorites. Without [force] it
     * does nothing once they have been downloaded.
     *
     * Throws [DataException] on failure; the favorites saved on the device stay as they are.
     */
    suspend fun refresh(force: Boolean)

    /**
     * Adds [movie] to the favorites, or removes it when it's already there. Saved on the device right away and synced
     * with the backend when it's reachable. Throws [DataException] when the backend rejects the change, which is then
     * reverted.
     */
    suspend fun toggle(movie: FavoriteMovie)
}
