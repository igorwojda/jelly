package com.igorwojda.jellymobile.feature.movie.domain.usecase

import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.repository.MovieRepository

/** Details of a single movie. See [MovieRepository.getMovieDetails]. */
internal class GetMovieDetailsUseCase(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(movieId: Long): MovieDetails = movieRepository.getMovieDetails(movieId)
}
