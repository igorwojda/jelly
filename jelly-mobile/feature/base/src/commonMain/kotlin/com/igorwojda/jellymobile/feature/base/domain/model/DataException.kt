package com.igorwojda.jellymobile.feature.base.domain.model

/**
 * Why a data request failed, whichever source served it (backend, TMDB, device). Repositories throw only this, so the
 * domain and presentation layers never depend on networking or parsing exceptions.
 */
sealed class DataException(
    cause: Throwable? = null,
) : Exception(cause) {
    /** No source could be reached: no connection or a timeout. */
    class NoConnection(
        cause: Throwable? = null,
    ) : DataException(cause)

    /** The requested data doesn't exist. */
    class NotFound(
        cause: Throwable? = null,
    ) : DataException(cause)

    /** The source was reached but failed to answer (server error). */
    class ServerUnavailable(
        cause: Throwable? = null,
    ) : DataException(cause)

    /** The source refused the request, e.g. invalid input. */
    class Rejected(
        cause: Throwable? = null,
    ) : DataException(cause)

    /** The source answered with data the app can't read. */
    class InvalidResponse(
        cause: Throwable? = null,
    ) : DataException(cause)

    /** Any other failure. */
    class Unknown(
        cause: Throwable? = null,
    ) : DataException(cause)
}
