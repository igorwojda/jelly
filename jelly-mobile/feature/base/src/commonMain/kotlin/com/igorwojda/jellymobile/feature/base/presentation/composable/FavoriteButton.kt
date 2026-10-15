package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/** Heart toggle that adds / removes a movie from favorites. */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
    ) {
        Icon(
            imageVector = if (isFavorite) JellyIcons.Favorite else JellyIcons.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun FavoriteButtonPreview() {
    JellyTheme {
        FavoriteButton(isFavorite = false, onClick = {})
    }
}

@Preview
@Composable
private fun FavoriteButtonSelectedPreview() {
    JellyTheme {
        FavoriteButton(isFavorite = true, onClick = {})
    }
}
