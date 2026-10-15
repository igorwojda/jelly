package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FakeFavoriteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ObserveFavoritesUseCaseTest {
    private val favoriteRepository = FakeFavoriteRepository(favorites = listOf(FavoriteMovie(id = 1, title = "Dune", posterUrl = null)))

    @Test
    fun `observes the repository favorites`() {
        val favorites = ObserveFavoritesUseCase(favoriteRepository)()

        assertSame(favoriteRepository.favorites, favorites)
        assertEquals(favoriteRepository.favorites.value, favorites.value)
    }
}
