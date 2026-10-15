package com.igorwojda.jellymobile.feature.base.domain.usecase

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSourceHealthTracker
import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveConnectionStatusUseCaseTest {
    private val healthTracker = RemoteSourceHealthTracker()
    private val observeConnectionStatus = ObserveConnectionStatusUseCase(healthTracker)

    @Test
    fun `emits the connection status of the remote sources`() =
        runTest {
            assertEquals(ConnectionStatus.ONLINE, observeConnectionStatus().first())

            healthTracker.reportUnavailable(RemoteSource.BACKEND)
            healthTracker.reportAvailable(RemoteSource.TMDB)

            assertEquals(ConnectionStatus.BACKEND_UNAVAILABLE, observeConnectionStatus().first())
        }
}
