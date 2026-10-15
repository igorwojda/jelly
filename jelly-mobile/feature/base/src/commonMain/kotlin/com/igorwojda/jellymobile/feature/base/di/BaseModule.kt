package com.igorwojda.jellymobile.feature.base.di

import com.igorwojda.jellymobile.feature.base.BuildConfig
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSourceHealthTracker
import com.igorwojda.jellymobile.feature.base.data.remote.jsonHttpClient
import com.igorwojda.jellymobile.feature.base.domain.repository.ConnectionStatusRepository
import com.igorwojda.jellymobile.feature.base.domain.usecase.ObserveConnectionStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Dependencies shared by all feature modules. */
val baseModule =
    module {
        includes(basePlatformModule)

        single(BaseQualifier.ApplicationScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

        // Data layer
        single(BaseQualifier.BackendHttpClient) { jsonHttpClient(baseUrl = BuildConfig.BACKEND_BASE_URL) }
        // Registered only when a TMDB token is configured; without it the app has no TMDB fallback.
        BuildConfig.TMDB_API_TOKEN.takeIf { it.isNotBlank() }?.let { token ->
            single(BaseQualifier.TmdbHttpClient) { jsonHttpClient(baseUrl = TMDB_BASE_URL, bearerToken = token) }
        }
        // Tracks the remote sources for the data layer; exposed to the domain as the connection status.
        single { RemoteSourceHealthTracker() } bind ConnectionStatusRepository::class

        // Domain layer
        factoryOf(::ObserveConnectionStatusUseCase)
    }

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
