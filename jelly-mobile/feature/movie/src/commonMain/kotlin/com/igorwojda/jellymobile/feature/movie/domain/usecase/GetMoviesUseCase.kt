package com.igorwojda.jellymobile.feature.movie.domain.usecase

import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import com.igorwojda.jellymobile.feature.movie.domain.repository.MovieRepository

/** One page of the movies in a category. See [MovieRepository.getMovies]. */
internal class GetMoviesUseCase(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(
        category: MovieCategory,
        page: Int,
    ): MoviePage = movieRepository.getMovies(category, page)
}
