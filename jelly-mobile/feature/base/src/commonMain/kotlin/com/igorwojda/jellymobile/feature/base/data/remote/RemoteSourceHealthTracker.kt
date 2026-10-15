package com.igorwojda.jellymobile.feature.base.data.remote

import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import com.igorwojda.jellymobile.feature.base.domain.repository.ConnectionStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Availability of each [RemoteSource], from the outcome of the latest request to it. Shared by every feature, so a
 * source found unavailable by one feature is skipped by the others.
 */
class RemoteSourceHealthTracker(
    /** How long an unavailable source is skipped before it's tried again. */
    private val retryAfter: Duration = 30.seconds,
    private val clock: Clock = Clock.System,
) : ConnectionStatusRepository {
    private val health = MutableStateFlow<Map<RemoteSource, SourceHealth>>(emptyMap())

    override val connectionStatus: Flow<ConnectionStatus> = health.map { it.toConnectionStatus() }.distinctUntilChanged()

    fun reportAvailable(source: RemoteSource) = report(source, isAvailable = true)

    fun reportUnavailable(source: RemoteSource) = report(source, isAvailable = false)

    /** `true` while [source] failed less than [retryAfter] ago: requests skip it rather than wait for it to fail again. */
    fun isRecentlyUnavailable(source: RemoteSource): Boolean {
        val sourceHealth = health.value[source] ?: return false
        return !sourceHealth.isAvailable && clock.now() - sourceHealth.checkedAt < retryAfter
    }

    private fun report(
        source: RemoteSource,
        isAvailable: Boolean,
    ) = health.update { it + (source to SourceHealth(isAvailable, clock.now())) }

    private fun Map<RemoteSource, SourceHealth>.toConnectionStatus(): ConnectionStatus =
        when {
            // Unknown counts as available, so nothing is reported before the first request.
            get(RemoteSource.BACKEND)?.isAvailable != false -> ConnectionStatus.ONLINE
            get(RemoteSource.TMDB)?.isAvailable == true -> ConnectionStatus.BACKEND_UNAVAILABLE
            else -> ConnectionStatus.OFFLINE
        }

    private data class SourceHealth(
        val isAvailable: Boolean,
        val checkedAt: Instant,
    )
}
