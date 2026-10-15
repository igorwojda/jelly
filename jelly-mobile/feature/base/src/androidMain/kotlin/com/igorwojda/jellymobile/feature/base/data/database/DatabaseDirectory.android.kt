package com.igorwojda.jellymobile.feature.base.data.database

import android.content.Context

actual class DatabaseDirectory(
    private val context: Context,
) {
    actual fun filePath(name: String): String = context.getDatabasePath(name).absolutePath
}
