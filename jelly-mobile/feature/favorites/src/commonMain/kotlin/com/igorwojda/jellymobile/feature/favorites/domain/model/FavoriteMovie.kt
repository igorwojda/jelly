package com.igorwojda.jellymobile.feature.favorites.domain.model

data class FavoriteMovie(
    val id: Long,
    val title: String,
    /** Absolute poster URL, `null` when the movie has no poster. */
    val posterUrl: String?,
)
