package com.igorwojda.jellymobile.feature.base.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class CacheFallbackTest {
    private var cached: String? = null

    @Test
    fun `caches fresh data`() =
        runTest {
            val result = fetchWithCacheFallback(fetch = { "fresh" }, saveToCache = { cached = it }, readFromCache = { cached })

            assertEquals("fresh", result)
            assertEquals("fresh", cached)
        }

    @Test
    fun `returns cached data when the fetch fails`() =
        runTest {
            cached = "cached"

            val result =
                fetchWithCacheFallback(
                    fetch = { throw IOException("No internet") },
                    saveToCache = { cached = it },
                    readFromCache = { cached },
                )

            assertEquals("cached", result)
        }

    @Test
    fun `rethrows the failure when nothing is cached`() =
        runTest {
            assertFailsWith<IOException> {
                fetchWithCacheFallback<String>(
                    fetch = { throw IOException("No internet") },
                    saveToCache = { cached = it },
                    readFromCache = { null },
                )
            }
        }

    @Test
    fun `rethrows not found without reading the cache`() =
        runTest {
            val notFound = httpFailure(HttpStatusCode.NotFound)
            var isCacheRead = false

            assertFailsWith<ClientRequestException> {
                fetchWithCacheFallback(
                    fetch = { throw notFound },
                    saveToCache = { cached = it },
                    readFromCache = { "cached".also { isCacheRead = true } },
                )
            }
            assertFalse(isCacheRead)
        }

    @Test
    fun `returns cached data when the server fails`() =
        runTest {
            val serverError = httpFailure(HttpStatusCode.ServiceUnavailable)
            cached = "cached"

            val result = fetchWithCacheFallback(fetch = { throw serverError }, saveToCache = { cached = it }, readFromCache = { cached })

            assertEquals("cached", result)
        }
}
