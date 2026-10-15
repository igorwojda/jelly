package com.igorwojda.jellymobile.feature.base.data.remote

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs a repository operation, rethrowing any failure as a [DataException], so networking and parsing exceptions never
 * leave the data layer. Cancellation passes through.
 */
suspend inline fun <T> mapDataErrors(crossinline block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        throw e.toDataException()
    }

/** The [DataException] describing this failure of a data source. */
fun Throwable.toDataException(): DataException =
    when (this) {
        is DataException -> this
        is ResponseException ->
            when {
                response.status == HttpStatusCode.NotFound -> DataException.NotFound(this)
                response.status.value >= HttpStatusCode.InternalServerError.value -> DataException.ServerUnavailable(this)
                else -> DataException.Rejected(this)
            }
        // Covers no connection, unreachable server and timeouts.
        is IOException -> DataException.NoConnection(this)
        // Ktor wraps JSON parsing failures in ContentConvertException.
        is SerializationException, is ContentConvertException -> DataException.InvalidResponse(this)
        else -> DataException.Unknown(this)
    }
