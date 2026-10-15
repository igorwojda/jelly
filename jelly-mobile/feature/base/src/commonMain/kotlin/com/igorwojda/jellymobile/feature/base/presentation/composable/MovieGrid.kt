package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Adaptive grid of [MovieCard]s, so every movie list shares the same column width and spacing. */
@Composable
fun MovieGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        state = state,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Preview
@Composable
private fun MovieGridPreview() {
    JellyTheme {
        MovieGrid {
            itemsIndexed(listOf("The Shawshank Redemption", "The Godfather", "The Dark Knight")) { index, title ->
                MovieCard(
                    movieId = index.toLong(),
                    title = title,
                    posterUrl = null,
                    isFavorite = false,
                    onClick = {},
                    onFavoriteClick = {},
                )
            }
        }
    }
}
