package com.igorwojda.jellymobile.feature.favorites.domain.usecase

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FakeFavoriteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class RefreshFavoritesUseCaseTest {
    private val favoriteRepository = FakeFavoriteRepository()
    private val refreshFavorites = RefreshFavoritesUseCase(favoriteRepository)

    @Test
    fun `forces the download by default`() =
        runTest {
            refreshFavorites()
            refreshFavorites(force = false)

            assertEquals(listOf(true, false), favoriteRepository.refreshes)
        }

    @Test
    fun `propagates the repository failure`() =
        runTest {
            favoriteRepository.refreshFailure = DataException.NoConnection()

            assertFailsWith<DataException.NoConnection> { refreshFavorites() }
        }
}
