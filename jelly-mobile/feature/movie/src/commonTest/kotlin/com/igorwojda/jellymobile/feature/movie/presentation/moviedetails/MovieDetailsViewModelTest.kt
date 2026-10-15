package com.igorwojda.jellymobile.feature.movie.presentation.moviedetails

import androidx.lifecycle.viewModelScope
import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.repository.FakeMovieRepository
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMovieDetailsUseCase
import com.igorwojda.jellymobile.feature.movie.presentation.FakeFavoriteRepository
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
class MovieDetailsViewModelTest {
    private val movieRepository = FakeMovieRepository()
    private val favoriteRepository = FakeFavoriteRepository()
    private var viewModel: MovieDetailsViewModel? = null
    private lateinit var state: MovieDetailsState
    private val actions = mutableListOf<MovieDetailsAction>()

    // The store runs in viewModelScope (Dispatchers.Main); unconfined, so each intent is handled before the assertions.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `loads the movie details`() =
        runTest {
            movieRepository.details[dune.id] = dune
            favoriteRepository.favorites.value = listOf(duneFavorite)

            createViewModel(dune.id)

            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = true), state)
            assertEquals(listOf(dune.id), movieRepository.detailsRequests)
        }

    @Test
    fun `refreshes the favorites without forcing a download`() =
        runTest {
            movieRepository.details[dune.id] = dune

            createViewModel(dune.id)

            assertEquals(listOf(false), favoriteRepository.refreshes)
        }

    @Test
    fun `shows the details when refreshing the favorites fails`() =
        runTest {
            movieRepository.details[dune.id] = dune
            favoriteRepository.refreshFailure = DataException.NoConnection()

            createViewModel(dune.id)

            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = false), state)
        }

    @Test
    fun `shows an error when loading fails and loads the details again on retry`() =
        runTest {
            movieRepository.failure = DataException.NoConnection()
            createViewModel(dune.id)

            assertEquals(MovieDetailsState.Error("Can't reach the server. Check your internet connection and try again."), state)

            movieRepository.failure = null
            movieRepository.details[dune.id] = dune
            intent(MovieDetailsIntent.Retry)

            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = false), state)
        }

    @Test
    fun `updates the favorite state when the favorites change`() =
        runTest {
            movieRepository.details[dune.id] = dune
            createViewModel(dune.id)

            favoriteRepository.favorites.value = listOf(duneFavorite)
            runCurrent()
            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = true), state)

            favoriteRepository.favorites.value = emptyList()
            runCurrent()
            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = false), state)
        }

    @Test
    fun `toggles the movie as a favorite`() =
        runTest {
            movieRepository.details[dune.id] = dune
            createViewModel(dune.id)

            intent(MovieDetailsIntent.ToggleFavorite)

            assertEquals(listOf(duneFavorite), favoriteRepository.toggled)
            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = true), state)
            assertEquals(emptyList(), actions)
        }

    @Test
    fun `ignores toggling the favorite before the details are loaded`() =
        runTest {
            movieRepository.failure = DataException.NoConnection()
            createViewModel(dune.id)

            intent(MovieDetailsIntent.ToggleFavorite)

            assertEquals(emptyList(), favoriteRepository.toggled)
        }

    @Test
    fun `shows a message when toggling the favorite fails`() =
        runTest {
            movieRepository.details[dune.id] = dune
            favoriteRepository.toggleFailure = DataException.NoConnection()
            createViewModel(dune.id)

            intent(MovieDetailsIntent.ToggleFavorite)

            assertEquals(
                listOf<MovieDetailsAction>(
                    MovieDetailsAction.ShowMessage(
                        "Couldn't update favorites. Can't reach the server. Check your internet connection and try again.",
                    ),
                ),
                actions,
            )
            assertEquals(MovieDetailsState.Content(movie = dune, isFavorite = false), state)
        }

    private fun TestScope.createViewModel(movieId: Long) {
        val viewModel =
            MovieDetailsViewModel(
                movieId = movieId,
                getMovieDetailsUseCase = GetMovieDetailsUseCase(movieRepository),
                observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository),
                refreshFavoritesUseCase = RefreshFavoritesUseCase(favoriteRepository),
                toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            )
        this@MovieDetailsViewModelTest.viewModel = viewModel
        backgroundScope.subscribe(viewModel.store, consume = { actions += it }, render = { state = it })
        runCurrent()
    }

    private fun TestScope.intent(intent: MovieDetailsIntent) {
        checkNotNull(viewModel).store.intent(intent)
        runCurrent()
    }

    private companion object {
        val dune = MovieDetails(id = 7, title = "Dune", posterUrl = "https://example.com/dune.jpg", overview = "Spice.", releaseYear = 2021, rating = 7.8)
        val duneFavorite = FavoriteMovie(id = 7, title = "Dune", posterUrl = "https://example.com/dune.jpg")
    }
}
