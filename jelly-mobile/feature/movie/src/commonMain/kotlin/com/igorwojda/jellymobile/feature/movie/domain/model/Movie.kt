package com.igorwojda.jellymobile.feature.movie.domain.model

/** Movie as shown in lists. */
data class Movie(
    val id: Long,
    val title: String,
    /** Absolute poster URL, `null` when the movie has no poster. */
    val posterUrl: String?,
)
