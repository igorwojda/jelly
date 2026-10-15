package com.igorwojda.jellymobile.feature.base.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.time.Clock
import kotlin.time.Instant

/** The exception Ktor throws for a [status] response (e.g. `ClientRequestException` for 404). */
internal suspend fun httpFailure(status: HttpStatusCode): Exception {
    val httpClient = HttpClient(MockEngine { respondError(status) }) { expectSuccess = true }
    try {
        httpClient.get("https://example.com")
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        return e
    }
    error("Expected $status to fail")
}

internal class FakeClock(
    var now: Instant = Instant.fromEpochMilliseconds(0),
) : Clock {
    override fun now(): Instant = now
}
