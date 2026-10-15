package com.igorwojda.jellymobile.feature.movie.domain.usecase

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.repository.FakeMovieRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class GetMovieDetailsUseCaseTest {
    private val movieRepository = FakeMovieRepository()
    private val getMovieDetails = GetMovieDetailsUseCase(movieRepository)

    @Test
    fun `returns the details of the movie`() =
        runTest {
            val details = MovieDetails(id = 7, title = "Dune", posterUrl = null, overview = "Spice.", releaseYear = 2021, rating = 7.8)
            movieRepository.details[7] = details

            assertEquals(details, getMovieDetails(movieId = 7))
            assertEquals(listOf(7L), movieRepository.detailsRequests)
        }

    @Test
    fun `propagates the repository failure`() =
        runTest {
            movieRepository.failure = DataException.NoConnection()

            assertFailsWith<DataException.NoConnection> { getMovieDetails(movieId = 7) }
        }
}
