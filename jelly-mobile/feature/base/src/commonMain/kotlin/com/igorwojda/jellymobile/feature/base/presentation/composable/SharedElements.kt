package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Scope of the `SharedTransitionLayout` wrapping the navigation host; `null` outside of it (e.g. in previews). */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** Animated scope of the current navigation entry; `null` outside of navigation (e.g. in previews). */
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Poster clip while it flies between screens; matches the card and details poster corners. */
private val posterTransitionShape = RoundedCornerShape(12.dp)

/** Animates a movie poster between screens (list → details). No-op outside of a shared transition. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedMoviePoster(movieId: Long): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this

    return with(sharedTransitionScope) {
        this@sharedMoviePoster.sharedElement(
            sharedContentState = rememberSharedContentState(key = "movie-poster-$movieId"),
            animatedVisibilityScope = animatedVisibilityScope,
            clipInOverlayDuringTransition = OverlayClip(posterTransitionShape),
        )
    }
}

/**
 * Animates a movie title between screens (list → details). The text is scaled, as its style differs between
 * screens. No-op outside of a shared transition.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedMovieTitle(movieId: Long): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this

    return with(sharedTransitionScope) {
        this@sharedMovieTitle.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "movie-title-$movieId"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = scaleToBounds(),
        )
    }
}
