package com.igorwojda.jellymobile.feature.base.data.database

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** Application Support: app data that isn't user documents, kept across launches and backed up. */
actual class DatabaseDirectory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun filePath(name: String): String {
        val directory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
        return requireNotNull(directory?.path) { "Application Support directory is unavailable" } + "/$name"
    }
}
