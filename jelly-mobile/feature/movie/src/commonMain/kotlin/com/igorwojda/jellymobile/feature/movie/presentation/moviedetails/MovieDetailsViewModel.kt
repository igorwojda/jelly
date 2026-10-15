package com.igorwojda.jellymobile.feature.movie.presentation.moviedetails

import com.igorwojda.jellymobile.feature.base.coroutines.suspendRunCatching
import com.igorwojda.jellymobile.feature.base.presentation.error.toErrorMessage
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.configuredStore
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMovieDetailsUseCase
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private typealias Ctx = PipelineContext<MovieDetailsState, MovieDetailsIntent, MovieDetailsAction>

class MovieDetailsViewModel internal constructor(
    private val movieId: Long,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val refreshFavoritesUseCase: RefreshFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : StoreViewModel<MovieDetailsState, MovieDetailsIntent, MovieDetailsAction>() {
    override val store =
        configuredStore(initial = MovieDetailsState.Loading, name = "MovieDetails") {
            init {
                launch {
                    observeFavoritesUseCase().collect { favorites ->
                        val isFavorite = favorites.isFavorite()
                        updateState<MovieDetailsState.Content, _> { copy(isFavorite = isFavorite) }
                    }
                }
                // The favorite toggle needs the favorites; a failure only leaves it unset, the details still work.
                launch { suspendRunCatching { refreshFavoritesUseCase(force = false) } }

                loadDetails()
            }

            reduce { intent ->
                when (intent) {
                    MovieDetailsIntent.Retry -> loadDetails()
                    MovieDetailsIntent.ToggleFavorite -> toggleFavorite()
                }
            }
        }

    private suspend fun Ctx.loadDetails() {
        updateState { MovieDetailsState.Loading }

        suspendRunCatching { getMovieDetailsUseCase(movieId) }
            .onSuccess { movie ->
                updateState {
                    MovieDetailsState.Content(movie = movie, isFavorite = observeFavoritesUseCase().value.isFavorite())
                }
            }.onFailure { e -> updateState { MovieDetailsState.Error(e.toErrorMessage()) } }
    }

    private suspend fun Ctx.toggleFavorite() =
        withState<MovieDetailsState.Content, _> {
            val favorite = FavoriteMovie(movie.id, movie.title, movie.posterUrl)

            launch {
                suspendRunCatching { toggleFavoriteUseCase(favorite) }
                    .onFailure { e ->
                        action(MovieDetailsAction.ShowMessage("Couldn't update favorites. ${e.toErrorMessage()}"))
                    }
            }
        }

    private fun List<FavoriteMovie>.isFavorite() = any { it.id == movieId }
}

sealed interface MovieDetailsState : MVIState {
    data object Loading : MovieDetailsState

    data class Content(
        val movie: MovieDetails,
        val isFavorite: Boolean,
    ) : MovieDetailsState

    data class Error(
        val message: String,
    ) : MovieDetailsState
}

sealed interface MovieDetailsIntent : MVIIntent {
    /** Error screen's retry button. */
    data object Retry : MovieDetailsIntent

    data object ToggleFavorite : MovieDetailsIntent
}

sealed interface MovieDetailsAction : MVIAction {
    data class ShowMessage(
        val message: String,
    ) : MovieDetailsAction
}
