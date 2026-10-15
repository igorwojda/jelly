package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.ImageRequest

/** TMDB posters are 2:3. */
const val POSTER_ASPECT_RATIO = 2f / 3f

/**
 * Movie poster, or a placeholder icon when the movie has none.
 *
 * [placeholderUrl] is shown from the memory cache while [url] loads, e.g. the smaller list poster on the details
 * screen, so the poster never flashes blank.
 */
@Composable
fun MoviePoster(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderUrl: String? = null,
) {
    Box(
        modifier = modifier.aspectRatio(POSTER_ASPECT_RATIO).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            Icon(
                imageVector = JellyIcons.Movie,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val request =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(url)
                    .placeholderMemoryCacheKey(placeholderUrl?.let(MemoryCache::Key))
                    .build()

            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Preview
@Composable
private fun MoviePosterPlaceholderPreview() {
    JellyTheme {
        MoviePoster(url = null, contentDescription = null, modifier = Modifier.width(150.dp))
    }
}
