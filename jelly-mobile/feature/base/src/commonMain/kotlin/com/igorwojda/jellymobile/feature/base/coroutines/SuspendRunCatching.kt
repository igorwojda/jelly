package com.igorwojda.jellymobile.feature.base.coroutines

import kotlin.coroutines.cancellation.CancellationException

/** [runCatching] for suspend calls: rethrows [CancellationException], so cancelled coroutines stay cancelled. */
suspend inline fun <T> suspendRunCatching(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        Result.failure(e)
    }
