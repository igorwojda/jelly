package com.igorwojda.jellymobile.feature.favorites.data.datasource.local

import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Client-generated user id the backend keys favorites by (there is no authentication).
 * Generated on first use and persisted, so favorites survive app restarts.
 */
internal class UserIdLocalDataSource(
    private val settings: Settings,
) {
    @OptIn(ExperimentalUuidApi::class)
    val userId: String by lazy {
        settings.getStringOrNull(KEY_USER_ID)
            ?: Uuid.random().toString().also { settings.putString(KEY_USER_ID, it) }
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
    }
}
