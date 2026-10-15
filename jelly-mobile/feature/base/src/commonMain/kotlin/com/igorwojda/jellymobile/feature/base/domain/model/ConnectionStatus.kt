package com.igorwojda.jellymobile.feature.base.domain.model

/** Where the app currently gets its data from. */
enum class ConnectionStatus {
    /** The Jelly backend is reachable. */
    ONLINE,

    /** The backend is unreachable: movies come from TMDB directly, favorites are saved on the device until it's back. */
    BACKEND_UNAVAILABLE,

    /** No remote source is reachable: only cached movies and locally saved favorites are available. */
    OFFLINE,
}
