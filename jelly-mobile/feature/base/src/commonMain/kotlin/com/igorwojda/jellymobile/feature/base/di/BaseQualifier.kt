package com.igorwojda.jellymobile.feature.base.di

import org.koin.core.qualifier.named

/** Koin qualifiers of base dependencies registered more than once per type. */
object BaseQualifier {
    /** `HttpClient` calling the Jelly backend. */
    val BackendHttpClient = named("BackendHttpClient")

    /** `HttpClient` calling the TMDB API directly. Not registered when no TMDB token is configured. */
    val TmdbHttpClient = named("TmdbHttpClient")

    /** `CoroutineScope` living as long as the app, for work that outlives a screen. */
    val ApplicationScope = named("ApplicationScope")
}
