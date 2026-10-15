package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.StateFlow

/** Favorites, most recently added first. See [FavoriteRepository.favorites]. */
class ObserveFavoritesUseCase(
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(): StateFlow<List<FavoriteMovie>> = favoriteRepository.favorites
}
