package com.igorwojda.jellymobile.feature.base.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * `HttpClient` for a JSON API at [baseUrl], authorized with [bearerToken] when given. [engine] defaults to the
 * platform engine; tests pass a mock one.
 */
fun jsonHttpClient(
    baseUrl: String,
    bearerToken: String? = null,
    engine: HttpClientEngine? = null,
): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        // Non-2xx responses throw ResponseException, so callers handle every failure the same way.
        expectSuccess = true

        defaultRequest {
            url(baseUrl)
            bearerToken?.let { bearerAuth(it) }
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            // Short connect timeout: an unreachable server should fall back to the next source quickly.
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        }
    }
    return if (engine == null) HttpClient(config) else HttpClient(engine, config)
}

private const val CONNECT_TIMEOUT_MILLIS = 5_000L
private const val REQUEST_TIMEOUT_MILLIS = 15_000L
