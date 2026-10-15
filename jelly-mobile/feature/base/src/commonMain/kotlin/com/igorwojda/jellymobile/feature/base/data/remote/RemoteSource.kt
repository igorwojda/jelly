package com.igorwojda.jellymobile.feature.base.data.remote

/** Remote services the app reads data from. */
enum class RemoteSource {
    /** Jelly backend, the primary source. */
    BACKEND,

    /** TMDB API called directly, the fallback when the backend is unavailable. */
    TMDB,
}
