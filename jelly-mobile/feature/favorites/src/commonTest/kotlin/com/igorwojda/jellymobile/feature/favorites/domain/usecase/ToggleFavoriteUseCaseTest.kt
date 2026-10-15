package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FakeFavoriteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class ToggleFavoriteUseCaseTest {
    private val favoriteRepository = FakeFavoriteRepository(favorites = listOf(dune))
    private val toggleFavorite = ToggleFavoriteUseCase(favoriteRepository)

    @Test
    fun `toggles the movie in the repository`() =
        runTest {
            toggleFavorite(alien)

            assertEquals(listOf(alien), favoriteRepository.toggled)
            assertEquals(listOf(alien, dune), favoriteRepository.favorites.value)
        }

    @Test
    fun `propagates the repository failure`() =
        runTest {
            favoriteRepository.toggleFailure = DataException.NoConnection()

            assertFailsWith<DataException.NoConnection> { toggleFavorite(alien) }
        }

    private companion object {
        val dune = FavoriteMovie(id = 1, title = "Dune", posterUrl = null)
        val alien = FavoriteMovie(id = 2, title = "Alien", posterUrl = null)
    }
}
