package com.igorwojda.jellymobile.feature.movie.data.model.backend

import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieDetailsResponseModel(
    val id: Long,
    val title: String,
    val posterUrl: String? = null,
    val overview: String,
    val releaseYear: Int? = null,
    val rating: Double,
)

internal fun MovieDetailsResponseModel.toDomain() =
    MovieDetails(
        id = id,
        title = title,
        posterUrl = posterUrl,
        overview = overview,
        releaseYear = releaseYear,
        rating = rating,
    )
