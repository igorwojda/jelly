package com.igorwojda.jellymobile.feature.favorites.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSource
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie

/**
 * Remote favorites of the user. The Jelly backend is the only implementation: TMDB keeps favorites per TMDB account,
 * which would need the user to sign in to TMDB, so without the backend favorites live on the device only.
 */
internal interface FavoriteRemoteDataSource : RemoteDataSource {
    /** Favorites, most recently added first. */
    suspend fun getFavorites(): List<FavoriteMovie>

    /** Adds the movie; succeeds when it's already a favorite. */
    suspend fun add(movieId: Long)

    /** Removes the movie; succeeds when it isn't a favorite. */
    suspend fun remove(movieId: Long)
}
