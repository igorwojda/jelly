package com.igorwojda.jellymobile.feature.base.data.remote

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class DataExceptionMapperTest {
    @Test
    fun `maps a connection failure`() {
        assertIs<DataException.NoConnection>(IOException("Connection refused").toDataException())
    }

    @Test
    fun `maps not found`() =
        runTest {
            assertIs<DataException.NotFound>(httpFailure(HttpStatusCode.NotFound).toDataException())
        }

    @Test
    fun `maps a server error`() =
        runTest {
            assertIs<DataException.ServerUnavailable>(httpFailure(HttpStatusCode.ServiceUnavailable).toDataException())
        }

    @Test
    fun `maps other client errors as rejected`() =
        runTest {
            assertIs<DataException.Rejected>(httpFailure(HttpStatusCode.BadRequest).toDataException())
        }

    @Test
    fun `maps an unexpected response`() {
        assertIs<DataException.InvalidResponse>(SerializationException("Missing field").toDataException())
    }

    @Test
    fun `maps any other failure as unknown`() {
        assertIs<DataException.Unknown>(IllegalStateException().toDataException())
    }

    @Test
    fun `keeps a data exception as is`() {
        val notFound = DataException.NotFound()

        assertSame(notFound, notFound.toDataException())
    }

    @Test
    fun `mapDataErrors rethrows failures as data exceptions`() =
        runTest {
            assertFailsWith<DataException.NoConnection> { mapDataErrors { throw IOException("No internet") } }
        }

    @Test
    fun `mapDataErrors lets cancellation through`() =
        runTest {
            assertFailsWith<CancellationException> { mapDataErrors { throw CancellationException("Cancelled") } }
        }
}
