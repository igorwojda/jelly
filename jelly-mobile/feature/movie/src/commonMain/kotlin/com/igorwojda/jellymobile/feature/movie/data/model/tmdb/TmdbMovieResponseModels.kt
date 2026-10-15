package com.igorwojda.jellymobile.feature.movie.data.model.tmdb

import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

// Mapped the same way as the backend does, so movies look the same whichever source serves them.

@Serializable
internal data class TmdbMoviePageResponseModel(
    val page: Int,
    @SerialName("total_pages") val totalPages: Int,
    val results: List<TmdbMovieResponseModel>,
)

@Serializable
internal data class TmdbMovieResponseModel(
    val id: Long,
    val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
)

@Serializable
internal data class TmdbMovieDetailsResponseModel(
    val id: Long,
    val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    val overview: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

internal fun TmdbMoviePageResponseModel.toDomain() =
    MoviePage(
        page = page,
        totalPages = minOf(totalPages, TMDB_MAX_PAGE),
        movies = results.map { Movie(id = it.id, title = it.title, posterUrl = posterUrl(it.posterPath, LIST_POSTER_SIZE)) },
    )

internal fun TmdbMovieDetailsResponseModel.toDomain() =
    MovieDetails(
        id = id,
        title = title,
        posterUrl = posterUrl(posterPath, DETAILS_POSTER_SIZE),
        overview = overview.orEmpty(),
        // TMDB returns "YYYY-MM-DD", or an empty string when the date is unknown.
        releaseYear = releaseDate?.take(4)?.toIntOrNull(),
        rating = (voteAverage * 10).roundToInt() / 10.0,
    )

private fun posterUrl(
    posterPath: String?,
    size: String,
): String? = posterPath?.takeIf { it.isNotBlank() }?.let { "$IMAGE_BASE_URL/$size$it" }

/** TMDB rejects pages above 500 even when `total_pages` is higher. */
private const val TMDB_MAX_PAGE = 500
private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
private const val LIST_POSTER_SIZE = "w342"
private const val DETAILS_POSTER_SIZE = "w780"
