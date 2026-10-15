package com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist

import com.igorwojda.jellymobile.feature.base.coroutines.suspendRunCatching
import com.igorwojda.jellymobile.feature.base.presentation.error.toErrorMessage
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.jellymobile.feature.base.presentation.flowmvi.configuredStore
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private typealias Ctx = PipelineContext<FavoriteListState, FavoriteListIntent, FavoriteListAction>

class FavoriteListViewModel internal constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val refreshFavoritesUseCase: RefreshFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : StoreViewModel<FavoriteListState, FavoriteListIntent, FavoriteListAction>() {
    override val store =
        configuredStore(initial = FavoriteListState.Loading, name = "FavoriteList") {
            init {
                // Favorites saved on the device show right away, also offline, and changes made on other screens (movie
                // list, details) show up here immediately.
                launch {
                    observeFavoritesUseCase().collect { movies -> updateState { FavoriteListState.Content(movies) } }
                }
                // A failed sync keeps the saved favorites; the connection status banner tells the user about it.
                launch { suspendRunCatching { refreshFavoritesUseCase() } }
            }

            reduce { intent ->
                when (intent) {
                    is FavoriteListIntent.ToggleFavorite -> toggleFavorite(intent.movie)
                }
            }
        }

    private fun Ctx.toggleFavorite(movie: FavoriteMovie) {
        launch {
            suspendRunCatching { toggleFavoriteUseCase(movie) }
                .onFailure { e -> action(FavoriteListAction.ShowMessage("Couldn't update favorites. ${e.toErrorMessage()}")) }
        }
    }
}

sealed interface FavoriteListState : MVIState {
    data object Loading : FavoriteListState

    data class Content(
        val movies: List<FavoriteMovie>,
    ) : FavoriteListState
}

sealed interface FavoriteListIntent : MVIIntent {
    data class ToggleFavorite(
        val movie: FavoriteMovie,
    ) : FavoriteListIntent
}

sealed interface FavoriteListAction : MVIAction {
    data class ShowMessage(
        val message: String,
    ) : FavoriteListAction
}
