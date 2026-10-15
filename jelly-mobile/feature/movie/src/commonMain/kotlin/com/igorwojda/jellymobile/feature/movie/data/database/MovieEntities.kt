package com.igorwojda.jellymobile.feature.movie.data.database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails

/** Movie as shown in lists, shared by every category page it appears on. */
@Entity(tableName = "movie")
internal data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val posterUrl: String?,
)

/** A cached page of a category. Stored even when it has no movies, so an empty page is still served offline. */
@Entity(tableName = "category_page", primaryKeys = ["category", "page"])
internal data class CategoryPageEntity(
    val category: MovieCategory,
    val page: Int,
    val totalPages: Int,
)

/** Movie at [position] on a cached [CategoryPageEntity]. */
@Entity(
    tableName = "category_page_movie",
    primaryKeys = ["category", "page", "position"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryPageEntity::class,
            parentColumns = ["category", "page"],
            childColumns = ["category", "page"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("movieId")],
)
internal data class CategoryPageMovieEntity(
    val category: MovieCategory,
    val page: Int,
    val position: Int,
    val movieId: Long,
)

@Entity(tableName = "movie_details")
internal data class MovieDetailsEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val posterUrl: String?,
    val overview: String,
    val releaseYear: Int?,
    val rating: Double,
)

internal fun Movie.toEntity() = MovieEntity(id = id, title = title, posterUrl = posterUrl)

internal fun MovieEntity.toDomain() = Movie(id = id, title = title, posterUrl = posterUrl)

internal fun MovieDetails.toEntity() =
    MovieDetailsEntity(
        id = id,
        title = title,
        posterUrl = posterUrl,
        overview = overview,
        releaseYear = releaseYear,
        rating = rating,
    )

internal fun MovieDetailsEntity.toDomain() =
    MovieDetails(
        id = id,
        title = title,
        posterUrl = posterUrl,
        overview = overview,
        releaseYear = releaseYear,
        rating = rating,
    )
