package com.igorwojda.jellymobile.feature.base.domain.usecase

import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import com.igorwojda.jellymobile.feature.base.domain.repository.ConnectionStatusRepository
import kotlinx.coroutines.flow.Flow

/** Where the app currently gets its data from, so the UI can show when it works in a degraded mode. */
class ObserveConnectionStatusUseCase internal constructor(
    private val connectionStatusRepository: ConnectionStatusRepository,
) {
    operator fun invoke(): Flow<ConnectionStatus> = connectionStatusRepository.connectionStatus
}
