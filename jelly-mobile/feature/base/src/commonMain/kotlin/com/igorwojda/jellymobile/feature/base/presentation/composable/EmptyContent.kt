package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

/** Full-screen empty state. */
@Composable
fun EmptyContent(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    MessageContent(icon = icon, title = title, message = message, modifier = modifier)
}

@Preview
@Composable
private fun EmptyContentPreview() {
    JellyTheme {
        EmptyContent(icon = JellyIcons.FavoriteBorder, title = "No favorites yet", message = "Tap the heart on a movie.")
    }
}
