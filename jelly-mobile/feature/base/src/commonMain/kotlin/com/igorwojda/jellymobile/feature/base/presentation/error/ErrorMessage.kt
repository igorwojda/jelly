package com.igorwojda.jellymobile.feature.base.presentation.error

import com.igorwojda.jellymobile.feature.base.domain.model.DataException

/** User-facing description of a failed request. Repositories throw [DataException]; anything else is unexpected. */
fun Throwable.toErrorMessage(): String =
    when (this) {
        is DataException.NoConnection -> "Can't reach the server. Check your internet connection and try again."
        is DataException.NotFound -> "Not found."
        is DataException.ServerUnavailable -> "The server is unavailable. Try again later."
        is DataException.Rejected -> "The request was rejected."
        is DataException.InvalidResponse -> "Unexpected server response."
        else -> "Something went wrong."
    }
