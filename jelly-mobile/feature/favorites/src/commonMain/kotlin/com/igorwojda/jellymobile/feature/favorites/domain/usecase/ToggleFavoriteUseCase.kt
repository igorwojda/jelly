package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FavoriteRepository

/** Adds [FavoriteMovie] to favorites, or removes it when it's already there. See [FavoriteRepository.toggle]. */
class ToggleFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(movie: FavoriteMovie) = favoriteRepository.toggle(movie)
}
