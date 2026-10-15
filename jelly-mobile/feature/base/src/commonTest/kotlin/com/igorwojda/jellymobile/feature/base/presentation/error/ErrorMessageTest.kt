package com.igorwojda.jellymobile.feature.base.presentation.error

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessageTest {
    @Test
    fun `maps a connection failure`() {
        assertEquals(
            "Can't reach the server. Check your internet connection and try again.",
            DataException.NoConnection().toErrorMessage(),
        )
    }

    @Test
    fun `maps not found`() {
        assertEquals("Not found.", DataException.NotFound().toErrorMessage())
    }

    @Test
    fun `maps a server error`() {
        assertEquals("The server is unavailable. Try again later.", DataException.ServerUnavailable().toErrorMessage())
    }

    @Test
    fun `maps a rejected request`() {
        assertEquals("The request was rejected.", DataException.Rejected().toErrorMessage())
    }

    @Test
    fun `maps an unexpected response`() {
        assertEquals("Unexpected server response.", DataException.InvalidResponse().toErrorMessage())
    }

    @Test
    fun `maps an unknown data failure`() {
        assertEquals("Something went wrong.", DataException.Unknown().toErrorMessage())
    }

    @Test
    fun `maps any other failure`() {
        assertEquals("Something went wrong.", IllegalStateException().toErrorMessage())
    }
}
