package com.igorwojda.jellymobile.feature.movie.presentation.movielist

import com.igorwojda.jellymobile.feature.base.coroutines.suspendRunCatching
import com.igorwojda.jellymobile.feature.base.presentation.error.toErrorMessage
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.configuredStore
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMoviesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private typealias Ctx = PipelineContext<MovieListState, MovieListIntent, MovieListAction>

/** Paged movies of the selected [MovieCategory], loading the next page as the user scrolls. */
class MovieListViewModel internal constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val refreshFavoritesUseCase: RefreshFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : StoreViewModel<MovieListState, MovieListIntent, MovieListAction>() {
    /** Loads the first page; cancelled when the user picks another category. */
    private var firstPageJob: Job? = null
    private var nextPageJob: Job? = null

    override val store =
        configuredStore(initial = MovieListState.Loading(MovieCategory.NOW_PLAYING), name = "MovieList") {
            init {
                launch {
                    observeFavoritesUseCase().collect { favorites ->
                        val favoriteIds = favorites.ids()
                        updateState<MovieListState.Content, _> { copy(favoriteIds = favoriteIds) }
                    }
                }
                // Hearts on the list need the favorites; a failure only leaves them empty, the list still works.
                launch { suspendRunCatching { refreshFavoritesUseCase(force = false) } }

                loadFirstPage(MovieCategory.NOW_PLAYING)
            }

            reduce { intent ->
                when (intent) {
                    is MovieListIntent.SelectCategory ->
                        withState { if (intent.category != category) loadFirstPage(intent.category) }
                    MovieListIntent.Retry -> withState { loadFirstPage(category) }
                    MovieListIntent.LoadNextPage -> loadNextPage()
                    is MovieListIntent.ToggleFavorite -> toggleFavorite(intent.movie)
                }
            }
        }

    private fun Ctx.loadFirstPage(category: MovieCategory) {
        firstPageJob?.cancel()
        nextPageJob?.cancel()

        firstPageJob =
            launch {
                updateState { MovieListState.Loading(category) }

                suspendRunCatching { getMoviesUseCase(category, page = 1) }
                    .onSuccess { page ->
                        updateState {
                            MovieListState.Content(
                                category = category,
                                movies = page.movies.distinctBy { it.id },
                                favoriteIds = observeFavoritesUseCase().value.ids(),
                                page = page.page,
                                totalPages = page.totalPages,
                            )
                        }
                    }.onFailure { e -> updateState { MovieListState.Error(category, e.toErrorMessage()) } }
            }
    }

    private suspend fun Ctx.loadNextPage() =
        withState<MovieListState.Content, _> {
            if (!canLoadMore || nextPageJob?.isActive == true) return@withState
            val content = this

            nextPageJob =
                launch {
                    updateState<MovieListState.Content, _> { copy(isLoadingNextPage = true, isNextPageError = false) }

                    suspendRunCatching { getMoviesUseCase(content.category, page = content.page + 1) }
                        .onSuccess { page ->
                            updateState<MovieListState.Content, _> {
                                copy(
                                    // Pages can overlap when the ranking changes between requests.
                                    movies = (movies + page.movies).distinctBy { it.id },
                                    page = page.page,
                                    totalPages = page.totalPages,
                                    isLoadingNextPage = false,
                                )
                            }
                        }.onFailure {
                            updateState<MovieListState.Content, _> { copy(isLoadingNextPage = false, isNextPageError = true) }
                        }
                }
        }

    private fun Ctx.toggleFavorite(movie: Movie) {
        launch {
            suspendRunCatching { toggleFavoriteUseCase(FavoriteMovie(movie.id, movie.title, movie.posterUrl)) }
                .onFailure { e -> action(MovieListAction.ShowMessage("Couldn't update favorites. ${e.toErrorMessage()}")) }
        }
    }

    private fun List<FavoriteMovie>.ids() = mapTo(mutableSetOf()) { it.id }
}

sealed interface MovieListState : MVIState {
    val category: MovieCategory

    data class Loading(
        override val category: MovieCategory,
    ) : MovieListState

    data class Content(
        override val category: MovieCategory,
        val movies: List<Movie>,
        val favoriteIds: Set<Long>,
        /** Last loaded page. */
        val page: Int,
        val totalPages: Int,
        val isLoadingNextPage: Boolean = false,
        /** Loading the next page failed; the loaded movies stay on screen with a retry option. */
        val isNextPageError: Boolean = false,
    ) : MovieListState {
        val canLoadMore: Boolean get() = page < totalPages
    }

    data class Error(
        override val category: MovieCategory,
        val message: String,
    ) : MovieListState
}

sealed interface MovieListIntent : MVIIntent {
    data class SelectCategory(
        val category: MovieCategory,
    ) : MovieListIntent

    /** Error screen's retry button. */
    data object Retry : MovieListIntent

    /** The user scrolled close to the end of the list, or retries a failed page. */
    data object LoadNextPage : MovieListIntent

    data class ToggleFavorite(
        val movie: Movie,
    ) : MovieListIntent
}

sealed interface MovieListAction : MVIAction {
    data class ShowMessage(
        val message: String,
    ) : MovieListAction
}
