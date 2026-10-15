package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.favorites.domain.repository.FavoriteRepository

/** Syncs the favorites with the backend. See [FavoriteRepository.refresh]. */
class RefreshFavoritesUseCase(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(force: Boolean = true) = favoriteRepository.refresh(force)
}
