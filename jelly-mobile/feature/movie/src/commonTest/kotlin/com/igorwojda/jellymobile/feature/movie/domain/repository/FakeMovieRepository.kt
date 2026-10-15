package com.igorwojda.jellymobile.feature.movie.domain.repository

import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage

/** [MovieRepository] serving [pages] and [details], or throwing [failure] when set. Records every request. */
internal class FakeMovieRepository : MovieRepository {
    val pages = mutableMapOf<Pair<MovieCategory, Int>, MoviePage>()
    val details = mutableMapOf<Long, MovieDetails>()
    var failure: Exception? = null
    val movieRequests = mutableListOf<Pair<MovieCategory, Int>>()
    val detailsRequests = mutableListOf<Long>()

    override suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage {
        movieRequests += category to page
        failure?.let { throw it }
        return pages.getValue(category to page)
    }

    override suspend fun getMovieDetails(movieId: Long): MovieDetails {
        detailsRequests += movieId
        failure?.let { throw it }
        return details.getValue(movieId)
    }
}
