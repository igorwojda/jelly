package com.igorwojda.jellymobile.feature.base.data.remote

import kotlin.coroutines.cancellation.CancellationException

/**
 * Interchangeable [RemoteDataSource]s in priority order, e.g. the Jelly backend, then TMDB directly. A request goes to
 * the first source that serves it, so swapping, adding or reordering sources doesn't change the repositories.
 *
 * Sources that recently failed are skipped (see [RemoteSourceHealthTracker.isRecentlyUnavailable]), so an unreachable
 * backend doesn't delay every request by its timeout.
 */
class RemoteDataSourceChain<S : RemoteDataSource>(
    private val dataSources: List<S>,
    private val healthTracker: RemoteSourceHealthTracker,
) {
    init {
        require(dataSources.isNotEmpty()) { "At least one remote data source is required" }
    }

    /**
     * Runs [request] against each source until one succeeds, then throws the failure of the last source tried.
     *
     * A client error (4xx) also moves on to the next source: an incomplete backend answers 404 for an endpoint it
     * doesn't implement, which is indistinguishable from a missing movie.
     */
    suspend fun <T> request(request: suspend (S) -> T): T {
        val candidates = dataSources.filterNot { healthTracker.isRecentlyUnavailable(it.source) }.ifEmpty { dataSources }
        var lastFailure: Exception? = null

        for (dataSource in candidates) {
            try {
                return request(dataSource).also { healthTracker.reportAvailable(dataSource.source) }
            } catch (e: CancellationException) {
                throw e
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                if (e.isSourceUnavailable()) {
                    healthTracker.reportUnavailable(dataSource.source)
                } else {
                    healthTracker.reportAvailable(dataSource.source)
                }
                lastFailure = e
            }
        }

        throw checkNotNull(lastFailure)
    }
}
