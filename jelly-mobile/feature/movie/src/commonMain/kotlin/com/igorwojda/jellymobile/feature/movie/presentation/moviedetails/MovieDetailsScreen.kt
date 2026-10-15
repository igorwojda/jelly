package com.igorwojda.jellymobile.feature.movie.presentation.moviedetails

    import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.igorwojda.jellymobile.feature.base.presentation.composable.ErrorContent
import com.igorwojda.jellymobile.feature.base.presentation.composable.FavoriteButton
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyIcons
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyTheme
import com.igorwojda.jellymobile.feature.base.presentation.composable.MoviePoster
import com.igorwojda.jellymobile.feature.base.presentation.composable.sharedMoviePoster
import com.igorwojda.jellymobile.feature.base.presentation.composable.sharedMovieTitle
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import kotlin.math.roundToInt

/**
 * Details of a movie. [listTitle] and [listPosterUrl] are already shown on the previous screen: they're displayed
 * (and animated) right away, while the details and the larger poster load.
 */
@Composable
fun MovieDetailsScreen(
    store: Store<MovieDetailsState, MovieDetailsIntent, MovieDetailsAction>,
    movieId: Long,
    listTitle: String,
    listPosterUrl: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is MovieDetailsAction.ShowMessage -> coroutineScope.launch { snackbarHostState.showSnackbar(action.message) }
        }
    }

    with(store) {
        MovieDetailsScreenContent(
            state = state,
            movieId = movieId,
            listTitle = listTitle,
            listPosterUrl = listPosterUrl,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

/** Stateless screen, so it can be previewed without a [Store]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntentReceiver<MovieDetailsIntent>.MovieDetailsScreenContent(
    state: MovieDetailsState,
    movieId: Long,
    listTitle: String,
    listPosterUrl: String?,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state as? MovieDetailsState.Content

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = JellyIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (content != null) {
                        FavoriteButton(
                            isFavorite = content.isFavorite,
                            onClick = { intent(MovieDetailsIntent.ToggleFavorite) },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MoviePoster(
                url = content?.movie?.posterUrl ?: listPosterUrl,
                placeholderUrl = listPosterUrl,
                contentDescription = content?.movie?.title ?: listTitle,
                modifier =
                    Modifier
                        .width(220.dp)
                        .sharedMoviePoster(movieId)
                        .clip(RoundedCornerShape(12.dp)),
            )

            Text(
                text = content?.movie?.title ?: listTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start).sharedMovieTitle(movieId),
            )

            when (state) {
                MovieDetailsState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))

                is MovieDetailsState.Error ->
                    ErrorContent(
                        message = state.message,
                        onRetry = { intent(MovieDetailsIntent.Retry) },
                    )

                is MovieDetailsState.Content -> FadeIn { MovieDetailsContent(movie = state.movie) }
            }
        }
    }
}

@Composable
private fun MovieDetailsContent(
    movie: MovieDetails,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            movie.releaseYear?.let { year ->
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = JellyIcons.Star,
                    contentDescription = "Rating",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(text = "${movie.rating.formatRating()} / 10", style = MaterialTheme.typography.titleMedium)
            }
        }

        Text(
            text = movie.overview.ifBlank { "No description available." },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** Fades [content] in (alpha 0 → 1) when it first appears, even if it's there from the first frame. */
@Composable
private fun FadeIn(content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(visibleState = visibleState, enter = fadeIn(tween(durationMillis = 500))) {
        content()
    }
}

/** One decimal place, e.g. `7.8`. */
private fun Double.formatRating(): String {
    val tenths = (this * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

@Preview
@Composable
private fun MovieDetailsScreenLoadingPreview() {
    MovieDetailsScreenPreview(state = MovieDetailsState.Loading)
}

@Preview
@Composable
private fun MovieDetailsScreenErrorPreview() {
    MovieDetailsScreenPreview(state = MovieDetailsState.Error(message = "No internet connection"))
}

@Preview
@Composable
private fun MovieDetailsScreenContentPreview() {
    MovieDetailsScreenPreview(state = MovieDetailsState.Content(movie = previewMovie, isFavorite = true))
}

@Composable
private fun MovieDetailsScreenPreview(state: MovieDetailsState) {
    JellyTheme {
        EmptyReceiver {
            MovieDetailsScreenContent(
                state = state,
                movieId = previewMovie.id,
                listTitle = previewMovie.title,
                listPosterUrl = null,
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
            )
        }
    }
}

@Preview
@Composable
private fun MovieDetailsContentPreview() {
    JellyTheme {
        MovieDetailsContent(movie = previewMovie)
    }
}

private val previewMovie =
    MovieDetails(
        id = 1,
        title = "The Shawshank Redemption",
        posterUrl = null,
        overview = "Imprisoned in the 1940s for the double murder of his wife and her lover...",
        releaseYear = 1994,
        rating = 8.7,
    )
