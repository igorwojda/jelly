package com.igorwojda.jellymobile.feature.base.data.image

import coil3.annotation.ExperimentalCoilApi
import coil3.network.CacheStrategy
import coil3.network.CacheStrategy.ReadResult
import coil3.network.CacheStrategy.WriteResult
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.request.Options
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Serves disk-cached images until they are older than [maxAge], then downloads them again.
 *
 * Ignores HTTP cache headers: posters rarely change, so a fixed lifetime saves network without serving stale images
 * forever.
 */
@OptIn(ExperimentalCoilApi::class)
class ImageExpiringCacheStrategy(
    private val maxAge: Duration = 1.days,
    private val clock: Clock = Clock.System,
) : CacheStrategy {
    override suspend fun read(
        cacheResponse: NetworkResponse,
        networkRequest: NetworkRequest,
        options: Options,
    ): ReadResult {
        val ageMillis = clock.now().toEpochMilliseconds() - cacheResponse.responseMillis
        val isExpired = ageMillis > maxAge.inWholeMilliseconds
        return if (isExpired) ReadResult(networkRequest) else ReadResult(cacheResponse)
    }

    override suspend fun write(
        cacheResponse: NetworkResponse?,
        networkRequest: NetworkRequest,
        networkResponse: NetworkResponse,
        options: Options,
    ): WriteResult = CacheStrategy.DEFAULT.write(cacheResponse, networkRequest, networkResponse, options)
}
