package com.igorwojda.jellymobile.feature.base.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

/**
 * `true` when the source couldn't answer: no connection, timeout, server error (5xx) or an unexpected response.
 * `false` when it answered with a client error (4xx), i.e. it's reachable.
 */
fun Throwable.isSourceUnavailable(): Boolean = this !is ClientRequestException

/** The source answered that the requested data doesn't exist (404). */
fun Throwable.isNotFound(): Boolean = this is ClientRequestException && response.status == HttpStatusCode.NotFound
