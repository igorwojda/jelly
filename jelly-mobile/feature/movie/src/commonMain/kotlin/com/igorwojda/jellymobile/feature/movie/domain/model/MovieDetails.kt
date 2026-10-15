package com.igorwojda.jellymobile.feature.movie.domain.model

data class MovieDetails(
    val id: Long,
    val title: String,
    /** Absolute poster URL, `null` when the movie has no poster. */
    val posterUrl: String?,
    val overview: String,
    /** `null` when the release date is unknown. */
    val releaseYear: Int?,
    /** Average user rating from 0 to 10. */
    val rating: Double,
)
