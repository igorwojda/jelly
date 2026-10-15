package com.igorwojda.jellymobile.feature.base.data.database

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** Platform directory holding the app's databases. */
expect class DatabaseDirectory {
    /** Absolute path of the database file [name]. */
    fun filePath(name: String): String
}

/** Builds the database with the bundled SQLite driver, so every platform runs the same SQLite version. */
fun <T : RoomDatabase> RoomDatabase.Builder<T>.buildWithBundledDriver(): T = setDriver(BundledSQLiteDriver()).build()
