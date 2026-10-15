package com.igorwojda.jellymobile.feature.base.data.remote

import kotlin.coroutines.cancellation.CancellationException

/**
 * Fetches fresh data with [fetch] and stores it with [saveToCache]. When the fetch fails, returns the copy from
 * [readFromCache] instead, or rethrows the failure when nothing is cached or the data doesn't exist (404).
 */
suspend fun <T : Any> fetchWithCacheFallback(
    fetch: suspend () -> T,
    saveToCache: suspend (T) -> Unit,
    readFromCache: suspend () -> T?,
): T {
    val fresh =
        try {
            fetch()
        } catch (e: CancellationException) {
            throw e
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            if (e.isNotFound()) throw e
            return readFromCache() ?: throw e
        }

    saveToCache(fresh)
    return fresh
}
