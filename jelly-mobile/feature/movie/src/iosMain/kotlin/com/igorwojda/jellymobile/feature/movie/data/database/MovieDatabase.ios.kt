package com.igorwojda.jellymobile.feature.movie.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual fun movieDatabaseBuilder(filePath: String): RoomDatabase.Builder<MovieDatabase> =
    Room.databaseBuilder<MovieDatabase>(name = filePath)
