package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Grid item of a movie list: poster, title and a favorite toggle. Used by the movie list and favorites. */
@Composable
fun MovieCard(
    movieId: Long,
    title: String,
    posterUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier) {
        Column {
            Box {
                MoviePoster(
                    url = posterUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().sharedMoviePoster(movieId),
                )
                FavoriteButton(
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).sharedMovieTitle(movieId),
            )
        }
    }
}

@Preview
@Composable
private fun MovieCardPreview() {
    JellyTheme {
        MovieCard(
            movieId = 1,
            title = "The Shawshank Redemption",
            posterUrl = null,
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.width(150.dp),
        )
    }
}
