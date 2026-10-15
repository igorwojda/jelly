package com.igorwojda.jellymobile.feature.movie.presentation.movielist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.igorwojda.jellymobile.feature.base.presentation.composable.ErrorContent
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyTheme
import com.igorwojda.jellymobile.feature.base.presentation.composable.LoadingContent
import com.igorwojda.jellymobile.feature.base.presentation.composable.MovieCard
import com.igorwojda.jellymobile.feature.base.presentation.composable.MovieGrid
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver

@Composable
fun MovieListScreen(
    store: Store<MovieListState, MovieListIntent, MovieListAction>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is MovieListAction.ShowMessage -> coroutineScope.launch { snackbarHostState.showSnackbar(action.message) }
        }
    }

    with(store) {
        MovieListScreenContent(
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
private fun IntentReceiver<MovieListIntent>.MovieListScreenContent(
    state: MovieListState,
    snackbarHostState: SnackbarHostState,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Movies") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            CategoryFilter(
                selected = state.category,
                onSelect = { intent(MovieListIntent.SelectCategory(it)) },
            )

            val contentModifier = Modifier.fillMaxSize()

            when (state) {
                is MovieListState.Loading -> LoadingContent(contentModifier)

                is MovieListState.Error ->
                    ErrorContent(
                        message = state.message,
                        onRetry = { intent(MovieListIntent.Retry) },
                        modifier = contentModifier,
                    )

                is MovieListState.Content ->
                    MovieListContent(
                        state = state,
                        onMovieClick = onMovieClick,
                        onFavoriteClick = { intent(MovieListIntent.ToggleFavorite(it)) },
                        onLoadNextPage = { intent(MovieListIntent.LoadNextPage) },
                        modifier = contentModifier,
                    )
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selected: MovieCategory,
    onSelect: (MovieCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MovieCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun MovieListContent(
    state: MovieListState.Content,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Created per category: the Content leaves the composition while another category loads, so it starts at the top.
    val gridState = rememberLazyGridState()

    LoadNextPageEffect(gridState = gridState, onLoadNextPage = onLoadNextPage)

    MovieGrid(modifier = modifier, state = gridState) {
        items(state.movies, key = { it.id }) { movie ->
            MovieCard(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.posterUrl,
                isFavorite = movie.id in state.favoriteIds,
                onClick = { onMovieClick(movie) },
                onFavoriteClick = { onFavoriteClick(movie) },
            )
        }

        if (state.isLoadingNextPage || state.isNextPageError) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                NextPageFooter(isError = state.isNextPageError, onRetry = onLoadNextPage)
            }
        }
    }
}

/** Requests the next page when the user scrolls within [PREFETCH_DISTANCE] items of the end of the list. */
@Composable
private fun LoadNextPageEffect(
    gridState: LazyGridState,
    onLoadNextPage: () -> Unit,
) {
    val currentOnLoadNextPage by rememberUpdatedState(onLoadNextPage)

    LaunchedEffect(gridState) {
        // Emits the item count while the end is near, so a loaded page that still doesn't fill the screen requests
        // the following one.
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow null
            layoutInfo.totalItemsCount.takeIf { lastVisibleIndex >= it - PREFETCH_DISTANCE }
        }.distinctUntilChanged()
            .filterNotNull()
            .collect { currentOnLoadNextPage() }
    }
}

@Composable
private fun NextPageFooter(
    isError: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        if (isError) {
            TextButton(onClick = onRetry) { Text("Couldn't load more movies. Retry") }
        } else {
            CircularProgressIndicator()
        }
    }
}

private val MovieCategory.label: String
    get() =
        when (this) {
            MovieCategory.NOW_PLAYING -> "Now Playing"
            MovieCategory.UPCOMING -> "Upcoming"
            MovieCategory.TOP_RATED -> "Top Rated"
        }

private const val PREFETCH_DISTANCE = 6

@Preview
@Composable
private fun MovieListScreenLoadingPreview() {
    JellyTheme {
        EmptyReceiver {
            MovieListScreenContent(
                state = MovieListState.Loading(MovieCategory.NOW_PLAYING),
                snackbarHostState = remember { SnackbarHostState() },
                onMovieClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MovieListScreenErrorPreview() {
    JellyTheme {
        EmptyReceiver {
            MovieListScreenContent(
                state = MovieListState.Error(MovieCategory.TOP_RATED, message = "No internet connection"),
                snackbarHostState = remember { SnackbarHostState() },
                onMovieClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MovieListScreenContentPreview() {
    JellyTheme {
        EmptyReceiver {
            MovieListScreenContent(
                state = previewContentState,
                snackbarHostState = remember { SnackbarHostState() },
                onMovieClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun CategoryFilterPreview() {
    JellyTheme {
        CategoryFilter(selected = MovieCategory.UPCOMING, onSelect = {})
    }
}

@Preview
@Composable
private fun MovieListContentPreview() {
    JellyTheme {
        MovieListContent(
            state = previewContentState.copy(isLoadingNextPage = true),
            onMovieClick = {},
            onFavoriteClick = {},
            onLoadNextPage = {},
        )
    }
}

@Preview
@Composable
private fun NextPageFooterLoadingPreview() {
    JellyTheme {
        NextPageFooter(isError = false, onRetry = {})
    }
}

@Preview
@Composable
private fun NextPageFooterErrorPreview() {
    JellyTheme {
        NextPageFooter(isError = true, onRetry = {})
    }
}

private val previewContentState =
    MovieListState.Content(
        category = MovieCategory.NOW_PLAYING,
        movies = (1L..6L).map { Movie(id = it, title = "Movie $it", posterUrl = null) },
        favoriteIds = setOf(2L),
        page = 1,
        totalPages = 3,
    )
