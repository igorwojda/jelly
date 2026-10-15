package com.igorwojda.jellymobile.feature.favorites.domain.repository

import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [FavoriteRepository]. Throws [refreshFailure] / [toggleFailure] when set and records every call. */
internal class FakeFavoriteRepository(
    favorites: List<FavoriteMovie> = emptyList(),
) : FavoriteRepository {
    override val favorites = MutableStateFlow(favorites)
    var refreshFailure: Exception? = null
    var toggleFailure: Exception? = null
    val refreshes = mutableListOf<Boolean>()
    val toggled = mutableListOf<FavoriteMovie>()

    override suspend fun refresh(force: Boolean) {
        refreshes += force
        refreshFailure?.let { throw it }
    }

    override suspend fun toggle(movie: FavoriteMovie) {
        toggled += movie
        toggleFailure?.let { throw it }
        favorites.update { favorites ->
            if (favorites.any { it.id == movie.id }) favorites.filterNot { it.id == movie.id } else listOf(movie) + favorites
        }
    }
}
