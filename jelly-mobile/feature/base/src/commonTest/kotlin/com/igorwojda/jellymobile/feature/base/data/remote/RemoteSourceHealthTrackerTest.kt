package com.igorwojda.jellymobile.feature.base.data.remote

import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RemoteSourceHealthTrackerTest {
    private val clock = FakeClock()
    private val healthTracker = RemoteSourceHealthTracker(retryAfter = 30.seconds, clock = clock)

    @Test
    fun `is online before the first request`() =
        runTest {
            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
            assertFalse(healthTracker.isRecentlyUnavailable(RemoteSource.BACKEND))
        }

    @Test
    fun `is online while the backend is available even when TMDB is not`() =
        runTest {
            healthTracker.reportUnavailable(RemoteSource.TMDB)
            healthTracker.reportAvailable(RemoteSource.BACKEND)

            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `reports the backend unavailable when only TMDB answers`() =
        runTest {
            healthTracker.reportUnavailable(RemoteSource.BACKEND)
            healthTracker.reportAvailable(RemoteSource.TMDB)

            assertEquals(ConnectionStatus.BACKEND_UNAVAILABLE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `is offline when the backend is unavailable and TMDB is unknown`() =
        runTest {
            healthTracker.reportUnavailable(RemoteSource.BACKEND)

            assertEquals(ConnectionStatus.OFFLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `is online again once the backend recovers`() =
        runTest {
            healthTracker.reportUnavailable(RemoteSource.BACKEND)
            healthTracker.reportUnavailable(RemoteSource.TMDB)

            healthTracker.reportAvailable(RemoteSource.BACKEND)

            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `treats an unavailable source as recently unavailable until the retry delay passes`() {
        healthTracker.reportUnavailable(RemoteSource.BACKEND)

        clock.now += 29.seconds
        assertTrue(healthTracker.isRecentlyUnavailable(RemoteSource.BACKEND))

        clock.now += 1.seconds
        assertFalse(healthTracker.isRecentlyUnavailable(RemoteSource.BACKEND))
    }

    @Test
    fun `available source is never recently unavailable`() {
        healthTracker.reportUnavailable(RemoteSource.BACKEND)
        healthTracker.reportAvailable(RemoteSource.BACKEND)

        assertFalse(healthTracker.isRecentlyUnavailable(RemoteSource.BACKEND))
    }
}
