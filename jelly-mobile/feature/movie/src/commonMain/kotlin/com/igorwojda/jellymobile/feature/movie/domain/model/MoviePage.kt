package com.igorwojda.jellymobile.feature.movie.domain.model

/** One page of a paged movie list. More pages are available while [page] < [totalPages]. */
data class MoviePage(
    val page: Int,
    val totalPages: Int,
    val movies: List<Movie>,
)
