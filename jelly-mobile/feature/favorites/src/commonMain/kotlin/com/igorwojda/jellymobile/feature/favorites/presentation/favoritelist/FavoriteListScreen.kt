package com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.igorwojda.jellymobile.feature.base.presentation.composable.EmptyContent
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyIcons
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyTheme
import com.igorwojda.jellymobile.feature.base.presentation.composable.LoadingContent
import com.igorwojda.jellymobile.feature.base.presentation.composable.MovieCard
import com.igorwojda.jellymobile.feature.base.presentation.composable.MovieGrid
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver

@Composable
fun FavoriteListScreen(
    store: Store<FavoriteListState, FavoriteListIntent, FavoriteListAction>,
    onMovieClick: (FavoriteMovie) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is FavoriteListAction.ShowMessage -> coroutineScope.launch { snackbarHostState.showSnackbar(action.message) }
        }
    }

    with(store) {
        FavoriteListScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onMovieClick = onMovieClick,
            modifier = modifier,
        )
    }
}

/** Stateless screen, so it can be previewed without a [Store]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntentReceiver<FavoriteListIntent>.FavoriteListScreenContent(
    state: FavoriteListState,
    snackbarHostState: SnackbarHostState,
    onMovieClick: (FavoriteMovie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Favorites") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        val contentModifier = Modifier.fillMaxSize().padding(contentPadding)

        when (state) {
            FavoriteListState.Loading -> LoadingContent(contentModifier)

            is FavoriteListState.Content ->
                FavoriteListContent(
                    movies = state.movies,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { intent(FavoriteListIntent.ToggleFavorite(it)) },
                    modifier = contentModifier,
                )
        }
    }
}

@Composable
private fun FavoriteListContent(
    movies: List<FavoriteMovie>,
    onMovieClick: (FavoriteMovie) -> Unit,
    onFavoriteClick: (FavoriteMovie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) {
        EmptyContent(
            icon = JellyIcons.FavoriteBorder,
            title = "No favorites yet",
            message = "Tap the heart on a movie to save it here.",
            modifier = modifier,
        )
        return
    }

    MovieGrid(modifier = modifier) {
        items(movies, key = { it.id }) { movie ->
            MovieCard(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.posterUrl,
                isFavorite = true,
                onClick = { onMovieClick(movie) },
                onFavoriteClick = { onFavoriteClick(movie) },
            )
        }
    }
}

@Preview
@Composable
private fun FavoriteListScreenLoadingPreview() {
    FavoriteListScreenPreview(state = FavoriteListState.Loading)
}

@Preview
@Composable
private fun FavoriteListScreenContentPreview() {
    FavoriteListScreenPreview(state = FavoriteListState.Content(movies = previewMovies))
}

@Composable
private fun FavoriteListScreenPreview(state: FavoriteListState) {
    JellyTheme {
        EmptyReceiver {
            FavoriteListScreenContent(
                state = state,
                snackbarHostState = remember { SnackbarHostState() },
                onMovieClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun FavoriteListContentPreview() {
    JellyTheme {
        FavoriteListContent(movies = previewMovies, onMovieClick = {}, onFavoriteClick = {})
    }
}

@Preview
@Composable
private fun FavoriteListContentEmptyPreview() {
    JellyTheme {
        FavoriteListContent(movies = emptyList(), onMovieClick = {}, onFavoriteClick = {})
    }
}

private val previewMovies =
    listOf(
        FavoriteMovie(id = 1, title = "The Shawshank Redemption", posterUrl = null),
        FavoriteMovie(id = 2, title = "The Godfather", posterUrl = null),
    )
