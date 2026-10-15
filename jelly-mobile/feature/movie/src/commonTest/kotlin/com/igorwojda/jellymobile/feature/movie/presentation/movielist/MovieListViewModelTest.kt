package com.igorwojda.jellymobile.feature.movie.presentation.movielist

import androidx.lifecycle.viewModelScope
import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import com.igorwojda.jellymobile.feature.movie.domain.repository.FakeMovieRepository
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMoviesUseCase
import com.igorwojda.jellymobile.feature.movie.presentation.FakeFavoriteRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
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
class MovieListViewModelTest {
    private val movieRepository = FakeMovieRepository()
    private val favoriteRepository = FakeFavoriteRepository()
    private var viewModel: MovieListViewModel? = null
    private lateinit var state: MovieListState
    private val actions = mutableListOf<MovieListAction>()

    // The store runs in viewModelScope (Dispatchers.Main); unconfined, so each intent is handled before the assertions.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `loads the first page of now playing movies`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 2, dune, alien)
            favoriteRepository.favorites.value = listOf(dune.toFavorite())

            createViewModel()

            assertEquals(
                MovieListState.Content(
                    category = MovieCategory.NOW_PLAYING,
                    movies = listOf(dune, alien),
                    favoriteIds = setOf(dune.id),
                    page = 1,
                    totalPages = 2,
                ),
                state,
            )
        }

    @Test
    fun `refreshes the favorites without forcing a download`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)

            createViewModel()

            assertEquals(listOf(false), favoriteRepository.refreshes)
        }

    @Test
    fun `shows the movies when refreshing the favorites fails`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            favoriteRepository.refreshFailure = DataException.NoConnection()

            createViewModel()

            assertEquals(listOf(dune), assertIs<MovieListState.Content>(state).movies)
        }

    @Test
    fun `shows an error when the first page fails and loads it again on retry`() =
        runTest {
            movieRepository.failure = DataException.NoConnection()
            createViewModel()

            assertEquals(
                MovieListState.Error(
                    MovieCategory.NOW_PLAYING,
                    "Can't reach the server. Check your internet connection and try again.",
                ),
                state,
            )

            movieRepository.failure = null
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            intent(MovieListIntent.Retry)

            assertEquals(listOf(dune), assertIs<MovieListState.Content>(state).movies)
        }

    @Test
    fun `loads the first page of the selected category`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            movieRepository.pages[MovieCategory.TOP_RATED to 1] = page(1, totalPages = 1, alien)
            createViewModel()

            intent(MovieListIntent.SelectCategory(MovieCategory.TOP_RATED))

            val content = assertIs<MovieListState.Content>(state)
            assertEquals(MovieCategory.TOP_RATED, content.category)
            assertEquals(listOf(alien), content.movies)
        }

    @Test
    fun `ignores selecting the current category`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            createViewModel()

            intent(MovieListIntent.SelectCategory(MovieCategory.NOW_PLAYING))

            assertEquals(listOf(MovieCategory.NOW_PLAYING to 1), movieRepository.movieRequests)
        }

    @Test
    fun `appends the next page without the movies already listed`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 3, dune, alien)
            // The ranking changed between requests: Alien moved to page 2.
            movieRepository.pages[MovieCategory.NOW_PLAYING to 2] = page(2, totalPages = 3, alien, heat)
            createViewModel()

            intent(MovieListIntent.LoadNextPage)

            val content = assertIs<MovieListState.Content>(state)
            assertEquals(listOf(dune, alien, heat), content.movies)
            assertEquals(2, content.page)
            assertTrue(content.canLoadMore)
        }

    @Test
    fun `doesn't load past the last page`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            createViewModel()

            intent(MovieListIntent.LoadNextPage)

            assertEquals(listOf(MovieCategory.NOW_PLAYING to 1), movieRepository.movieRequests)
        }

    @Test
    fun `keeps the loaded movies when the next page fails and loads it on retry`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 2, dune)
            createViewModel()
            movieRepository.failure = DataException.NoConnection()

            intent(MovieListIntent.LoadNextPage)

            val failed = assertIs<MovieListState.Content>(state)
            assertEquals(listOf(dune), failed.movies)
            assertEquals(true, failed.isNextPageError)
            assertEquals(false, failed.isLoadingNextPage)

            movieRepository.failure = null
            movieRepository.pages[MovieCategory.NOW_PLAYING to 2] = page(2, totalPages = 2, alien)
            intent(MovieListIntent.LoadNextPage)

            val loaded = assertIs<MovieListState.Content>(state)
            assertEquals(listOf(dune, alien), loaded.movies)
            assertEquals(false, loaded.isNextPageError)
        }

    @Test
    fun `updates the favorite movies when the favorites change`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune, alien)
            createViewModel()

            favoriteRepository.favorites.value = listOf(alien.toFavorite())
            runCurrent()

            assertEquals(setOf(alien.id), assertIs<MovieListState.Content>(state).favoriteIds)
        }

    @Test
    fun `toggles a favorite`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            createViewModel()

            intent(MovieListIntent.ToggleFavorite(dune))

            assertEquals(listOf(dune.toFavorite()), favoriteRepository.toggled)
            assertEquals(setOf(dune.id), assertIs<MovieListState.Content>(state).favoriteIds)
            assertEquals(emptyList(), actions)
        }

    @Test
    fun `shows a message when toggling a favorite fails`() =
        runTest {
            movieRepository.pages[MovieCategory.NOW_PLAYING to 1] = page(1, totalPages = 1, dune)
            favoriteRepository.toggleFailure = DataException.NoConnection()
            createViewModel()

            intent(MovieListIntent.ToggleFavorite(dune))

            assertEquals(
                listOf<MovieListAction>(
                    MovieListAction.ShowMessage(
                        "Couldn't update favorites. Can't reach the server. Check your internet connection and try again.",
                    ),
                ),
                actions,
            )
            assertEquals(emptySet(), assertIs<MovieListState.Content>(state).favoriteIds)
        }

    private fun TestScope.createViewModel() {
        val viewModel =
            MovieListViewModel(
                getMoviesUseCase = GetMoviesUseCase(movieRepository),
                observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository),
                refreshFavoritesUseCase = RefreshFavoritesUseCase(favoriteRepository),
                toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            )
        this@MovieListViewModelTest.viewModel = viewModel
        backgroundScope.subscribe(viewModel.store, consume = { actions += it }, render = { state = it })
        runCurrent()
    }

    private fun TestScope.intent(intent: MovieListIntent) {
        checkNotNull(viewModel).store.intent(intent)
        runCurrent()
    }

    private fun page(
        page: Int,
        totalPages: Int,
        vararg movies: Movie,
    ) = MoviePage(page = page, totalPages = totalPages, movies = movies.toList())

    private fun Movie.toFavorite() = FavoriteMovie(id = id, title = title, posterUrl = posterUrl)

    private companion object {
        val dune = Movie(id = 1, title = "Dune", posterUrl = null)
        val alien = Movie(id = 2, title = "Alien", posterUrl = null)
        val heat = Movie(id = 3, title = "Heat", posterUrl = null)
    }
}
