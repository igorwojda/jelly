package com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist

import androidx.lifecycle.viewModelScope
import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FakeFavoriteRepository
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.subscribe

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteListViewModelTest {
    private val favoriteRepository = FakeFavoriteRepository()
    private var viewModel: FavoriteListViewModel? = null
    private lateinit var state: FavoriteListState
    private val actions = mutableListOf<FavoriteListAction>()

    // The store runs in viewModelScope (Dispatchers.Main); unconfined, so each intent is handled before the assertions.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the favorites saved on the device`() =
        runTest {
            favoriteRepository.favorites.value = listOf(dune, alien)

            createViewModel()

            assertEquals(FavoriteListState.Content(listOf(dune, alien)), state)
        }

    @Test
    fun `syncs the favorites with the backend when opened`() =
        runTest {
            createViewModel()

            assertEquals(listOf(true), favoriteRepository.refreshes)
        }

    @Test
    fun `keeps showing the saved favorites when the sync fails`() =
        runTest {
            favoriteRepository.favorites.value = listOf(dune)
            favoriteRepository.refreshFailure = DataException.NoConnection()

            createViewModel()

            assertEquals(FavoriteListState.Content(listOf(dune)), state)
            assertEquals(emptyList(), actions)
        }

    @Test
    fun `shows favorites changed on other screens`() =
        runTest {
            createViewModel()

            favoriteRepository.favorites.value = listOf(alien)
            runCurrent()

            assertEquals(FavoriteListState.Content(listOf(alien)), state)
        }

    @Test
    fun `removes a favorite`() =
        runTest {
            favoriteRepository.favorites.value = listOf(dune, alien)
            createViewModel()

            intent(FavoriteListIntent.ToggleFavorite(dune))

            assertEquals(listOf(dune), favoriteRepository.toggled)
            assertEquals(FavoriteListState.Content(listOf(alien)), state)
        }

    @Test
    fun `shows a message when removing a favorite fails`() =
        runTest {
            favoriteRepository.favorites.value = listOf(dune)
            favoriteRepository.toggleFailure = DataException.NoConnection()
            createViewModel()

            intent(FavoriteListIntent.ToggleFavorite(dune))

            assertEquals(
                listOf<FavoriteListAction>(
                    FavoriteListAction.ShowMessage(
                        "Couldn't update favorites. Can't reach the server. Check your internet connection and try again.",
                    ),
                ),
                actions,
            )
            assertEquals(FavoriteListState.Content(listOf(dune)), state)
        }

    private fun TestScope.createViewModel() {
        val viewModel =
            FavoriteListViewModel(
                observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository),
                refreshFavoritesUseCase = RefreshFavoritesUseCase(favoriteRepository),
                toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            )
        this@FavoriteListViewModelTest.viewModel = viewModel
        backgroundScope.subscribe(viewModel.store, consume = { actions += it }, render = { state = it })
        runCurrent()
    }

    private fun TestScope.intent(intent: FavoriteListIntent) {
        checkNotNull(viewModel).store.intent(intent)
        runCurrent()
    }

    private companion object {
        val dune = FavoriteMovie(id = 1, title = "Dune", posterUrl = null)
        val alien = FavoriteMovie(id = 2, title = "Alien", posterUrl = null)
    }
}
