package com.igorwojda.jellymobile.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Route : NavKey

@Serializable
data object MovieListRoute : Route

@Serializable
data object FavoriteListRoute : Route

@Serializable
data class MovieDetailsRoute(
    val movieId: Long,
    /** Title and poster shown on the previous screen, displayed (and animated) while the details load. */
    val listTitle: String,
    val listPosterUrl: String?,
) : Route

/** Back stacks are saved as [NavKey]s; non-Android platforms need the subtypes registered for serialization. */
internal val routeSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(MovieListRoute::class, MovieListRoute.serializer())
                    subclass(FavoriteListRoute::class, FavoriteListRoute.serializer())
                    subclass(MovieDetailsRoute::class, MovieDetailsRoute.serializer())
                }
            }
    }
