package com.igorwojda.jellymobile.feature.movie.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/** Cache of the movies loaded from the network, served when no remote source is available. */
@Database(
    entities = [MovieEntity::class, CategoryPageEntity::class, CategoryPageMovieEntity::class, MovieDetailsEntity::class],
    version = 1,
)
@ConstructedBy(MovieDatabaseConstructor::class)
internal abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        const val FILE_NAME = "movies.db"
    }
}

// The Room compiler generates the actual implementations.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
internal expect object MovieDatabaseConstructor : RoomDatabaseConstructor<MovieDatabase> {
    override fun initialize(): MovieDatabase
}

/** Room's builder needs the reified database type, which only the platform `Room` APIs provide. */
internal expect fun movieDatabaseBuilder(filePath: String): RoomDatabase.Builder<MovieDatabase>
