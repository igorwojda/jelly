package com.igorwojda.jellymobile.feature.favorites.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual fun favoriteDatabaseBuilder(filePath: String): RoomDatabase.Builder<FavoriteDatabase> =
    Room.databaseBuilder<FavoriteDatabase>(name = filePath)
