package com.igorwojda.jellymobile.feature.base.data.remote

/** Data source backed by a [RemoteSource]. Interchangeable implementations are chained by [RemoteDataSourceChain]. */
interface RemoteDataSource {
    val source: RemoteSource
}
