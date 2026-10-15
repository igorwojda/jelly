package com.igorwojda.jellymobile.feature.base.domain.repository

import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

/** Where the app currently gets its data from. */
interface ConnectionStatusRepository {
    /** [ConnectionStatus.ONLINE] until a request fails. */
    val connectionStatus: Flow<ConnectionStatus>
}
