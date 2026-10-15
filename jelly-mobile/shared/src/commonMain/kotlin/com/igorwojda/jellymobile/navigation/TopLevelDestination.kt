package com.igorwojda.jellymobile.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyIcons

/** Bottom navigation tabs. Each tab keeps its own back stack, starting at [route]. */
internal enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Route,
) {
    MOVIES("Movies", JellyIcons.Movie, MovieListRoute),
    FAVORITES("Favorites", JellyIcons.Favorite, FavoriteListRoute),
}
