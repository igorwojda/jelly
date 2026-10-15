package com.igorwojda.jellymobile

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.igorwojda.jellymobile.feature.base.data.image.ImageExpiringCacheStrategy
import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import com.igorwojda.jellymobile.feature.base.domain.usecase.ObserveConnectionStatusUseCase
import com.igorwojda.jellymobile.feature.base.presentation.composable.ConnectionStatusBanner
import com.igorwojda.jellymobile.feature.base.presentation.composable.JellyTheme
import com.igorwojda.jellymobile.feature.base.presentation.composable.LocalNavAnimatedVisibilityScope
import com.igorwojda.jellymobile.feature.base.presentation.composable.LocalSharedTransitionScope
import com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist.FavoriteListScreen
import com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist.FavoriteListViewModel
import com.igorwojda.jellymobile.feature.movie.presentation.moviedetails.MovieDetailsScreen
import com.igorwojda.jellymobile.feature.movie.presentation.moviedetails.MovieDetailsViewModel
import com.igorwojda.jellymobile.feature.movie.presentation.movielist.MovieListScreen
import com.igorwojda.jellymobile.feature.movie.presentation.movielist.MovieListViewModel
import com.igorwojda.jellymobile.navigation.FavoriteListRoute
import com.igorwojda.jellymobile.navigation.MovieDetailsRoute
import com.igorwojda.jellymobile.navigation.MovieListRoute
import com.igorwojda.jellymobile.navigation.TopLevelDestination
import com.igorwojda.jellymobile.navigation.routeSavedStateConfiguration
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * App shell shared by Android and iOS: theme, bottom navigation and the Navigation 3 host.
 *
 * Every tab has its own back stack, so switching tabs keeps each tab's screens, scroll positions and ViewModels.
 * Screens cross-fade, while elements common to both screens (e.g. a movie poster) morph into each other.
 * A banner above the bottom navigation tells the user when data comes from a fallback (TMDB, or the device only).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader
            .Builder(context)
            .components { add(KtorNetworkFetcherFactory(cacheStrategy = { ImageExpiringCacheStrategy() })) }
            .build()
    }

    JellyTheme {
        var currentDestination by rememberSaveable { mutableStateOf(TopLevelDestination.MOVIES) }
        val backStacks =
            TopLevelDestination.entries.associateWith { destination ->
                rememberNavBackStack(routeSavedStateConfiguration, destination.route)
            }
        val currentBackStack = backStacks.getValue(currentDestination)

        val observeConnectionStatus = koinInject<ObserveConnectionStatusUseCase>()
        val connectionStatus by remember(observeConnectionStatus) { observeConnectionStatus() }
            .collectAsStateWithLifecycle(initialValue = ConnectionStatus.ONLINE)

        // Decorated per tab, so the entries (and their ViewModels) of the hidden tab stay alive.
        val entriesByDestination =
            backStacks.mapValues { (_, backStack) ->
                rememberDecoratedNavEntries(
                    backStack = backStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider = appEntryProvider(backStack),
                )
            }

        Scaffold(
            // Each screen handles the status bar insets in its own top app bar.
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                // Below the content rather than at the top, so it never covers the screens' top app bars.
                Column {
                    ConnectionStatusBanner(connectionStatus)
                    BottomNavigationBar(
                        currentDestination = currentDestination,
                        onDestinationClick = { destination ->
                            if (destination == currentDestination) {
                                backStacks.getValue(destination).popToRoot()
                            } else {
                                currentDestination = destination
                            }
                        },
                    )
                }
            },
        ) { contentPadding ->
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    NavDisplay(
                        entries = entriesByDestination.getValue(currentDestination),
                        onBack = { currentBackStack.removeLastOrNull() },
                        modifier = Modifier.padding(contentPadding).consumeWindowInsets(contentPadding),
                        transitionSpec = { crossFade() },
                        popTransitionSpec = { crossFade() },
                        predictivePopTransitionSpec = { crossFade() },
                    )
                }
            }
        }
    }
}

/** Screens of the app; navigation from a screen stays within the tab's [backStack]. */
private fun appEntryProvider(backStack: NavBackStack<NavKey>) =
    entryProvider<NavKey> {
        entry<MovieListRoute> {
            val movieListViewModel: MovieListViewModel = koinViewModel()

            WithNavAnimatedScope {
                MovieListScreen(
                    movieListViewModel.store,
                    onMovieClick = { movie -> backStack.add(MovieDetailsRoute(movie.id, movie.title, movie.posterUrl)) },
                )
            }
        }
        entry<FavoriteListRoute> {
            val favoriteListViewModel: FavoriteListViewModel = koinViewModel()

            WithNavAnimatedScope {
                FavoriteListScreen(
                    favoriteListViewModel.store,
                    onMovieClick = { movie -> backStack.add(MovieDetailsRoute(movie.id, movie.title, movie.posterUrl)) },
                )
            }
        }
        entry<MovieDetailsRoute> { route ->
            val movieDetailsViewModel: MovieDetailsViewModel =
                koinViewModel(key = "movie-details-${route.movieId}") { parametersOf(route.movieId) }

            WithNavAnimatedScope {
                MovieDetailsScreen(
                    movieDetailsViewModel.store,
                    movieId = route.movieId,
                    listTitle = route.listTitle,
                    listPosterUrl = route.listPosterUrl,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        }
    }

@Composable
private fun BottomNavigationBar(
    currentDestination: TopLevelDestination,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = { onDestinationClick(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(destination.label) },
            )
        }
    }
}

/** Exposes the navigation entry's animation scope to the shared element transitions of the feature screens. */
@Composable
private fun WithNavAnimatedScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides LocalNavAnimatedContentScope.current) {
        content()
    }
}

private fun crossFade(): ContentTransform = fadeIn() togetherWith fadeOut()

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeAt(lastIndex)
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    JellyTheme {
        BottomNavigationBar(currentDestination = TopLevelDestination.MOVIES, onDestinationClick = {})
    }
}
