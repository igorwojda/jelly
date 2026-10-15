package com.igorwojda.jellymobile.feature.movie.domain.repository

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage

/** Movie data. The implementation decides where it comes from (backend, TMDB, local cache). */
internal interface MovieRepository {
    /** One [page] (starting at 1) of the movies in [category]. Throws [DataException] on failure. */
    suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage

    /** Details of a single movie. Throws [DataException] on failure, [DataException.NotFound] for an unknown [movieId]. */
    suspend fun getMovieDetails(movieId: Long): MovieDetails
}
