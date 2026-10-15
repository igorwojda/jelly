package com.igorwojda.jellymobile.feature.movie.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSource
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage

/** Remote movie API. Implementations map their own response models to the same domain models, so they're swappable. */
internal interface MovieRemoteDataSource : RemoteDataSource {
    /** One [page] (starting at 1) of the movies in [category]. Throws on network / parsing failure. */
    suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage

    /** Details of a single movie. Throws on network / parsing failure, including an unknown [movieId] (404). */
    suspend fun getMovieDetails(movieId: Long): MovieDetails
}
