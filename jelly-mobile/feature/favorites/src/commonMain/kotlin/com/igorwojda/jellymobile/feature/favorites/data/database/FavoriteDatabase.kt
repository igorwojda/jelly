package com.igorwojda.jellymobile.feature.favorites.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/** Favorites saved on the device: the source of truth the UI observes, synced with the backend when it's reachable. */
@Database(entities = [FavoriteMovieEntity::class], version = 1)
@ConstructedBy(FavoriteDatabaseConstructor::class)
internal abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao

    companion object {
        const val FILE_NAME = "favorites.db"
    }
}

// The Room compiler generates the actual implementations.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
internal expect object FavoriteDatabaseConstructor : RoomDatabaseConstructor<FavoriteDatabase> {
    override fun initialize(): FavoriteDatabase
}

/** Room's builder needs the reified database type, which only the platform `Room` APIs provide. */
internal expect fun favoriteDatabaseBuilder(filePath: String): RoomDatabase.Builder<FavoriteDatabase>
