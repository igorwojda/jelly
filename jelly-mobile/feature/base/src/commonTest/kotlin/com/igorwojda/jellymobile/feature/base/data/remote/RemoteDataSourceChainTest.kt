package com.igorwojda.jellymobile.feature.base.data.remote

import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds

class RemoteDataSourceChainTest {
    private val clock = FakeClock()
    private val healthTracker = RemoteSourceHealthTracker(retryAfter = 30.seconds, clock = clock)
    private val backend = FakeDataSource(RemoteSource.BACKEND)
    private val tmdb = FakeDataSource(RemoteSource.TMDB)
    private val chain = RemoteDataSourceChain(listOf(backend, tmdb), healthTracker)

    @Test
    fun `uses the backend when it's available`() =
        runTest {
            val result = chain.request { it.fetch() }

            assertEquals("BACKEND", result)
            assertEquals(0, tmdb.requestCount)
            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `falls back to TMDB when the backend is unreachable`() =
        runTest {
            backend.failure = IOException("Connection refused")

            val result = chain.request { it.fetch() }

            assertEquals("TMDB", result)
            assertEquals(ConnectionStatus.BACKEND_UNAVAILABLE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `falls back to TMDB when the backend answers 404 for an endpoint it does not implement`() =
        runTest {
            backend.failure = httpFailure(HttpStatusCode.NotFound)

            val result = chain.request { it.fetch() }

            assertEquals("TMDB", result)
            // The backend answered, so it's still reachable.
            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `throws the last failure when every source fails`() =
        runTest {
            backend.failure = IOException("Connection refused")
            val tmdbFailure = IOException("No internet")
            tmdb.failure = tmdbFailure

            val thrown = assertFailsWith<IOException> { chain.request { it.fetch() } }

            assertSame(tmdbFailure, thrown)
            assertEquals(ConnectionStatus.OFFLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `skips a recently unavailable source until the retry delay passes`() =
        runTest {
            backend.failure = IOException("Connection refused")
            chain.request { it.fetch() }
            backend.failure = null

            clock.now += 10.seconds
            assertEquals("TMDB", chain.request { it.fetch() })
            assertEquals(1, backend.requestCount)

            clock.now += 30.seconds
            assertEquals("BACKEND", chain.request { it.fetch() })
            assertEquals(ConnectionStatus.ONLINE, healthTracker.connectionStatus.first())
        }

    @Test
    fun `tries a recently unavailable source when it's the only one`() =
        runTest {
            val singleSourceChain = RemoteDataSourceChain(listOf(backend), healthTracker)
            backend.failure = IOException("Connection refused")
            assertFailsWith<IOException> { singleSourceChain.request { it.fetch() } }
            backend.failure = null

            assertEquals("BACKEND", singleSourceChain.request { it.fetch() })
        }

    private class FakeDataSource(
        override val source: RemoteSource,
    ) : RemoteDataSource {
        var failure: Exception? = null
        var requestCount = 0

        fun fetch(): String {
            requestCount++
            failure?.let { throw it }
            return source.name
        }
    }
}
