package com.igorwojda.jellymobile.feature.movie.domain.usecase

import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import com.igorwojda.jellymobile.feature.movie.domain.repository.FakeMovieRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class GetMoviesUseCaseTest {
    private val movieRepository = FakeMovieRepository()
    private val getMovies = GetMoviesUseCase(movieRepository)

    @Test
    fun `returns the requested page of the category`() =
        runTest {
            val page = MoviePage(page = 2, totalPages = 3, movies = listOf(Movie(id = 1, title = "Dune", posterUrl = null)))
            movieRepository.pages[MovieCategory.TOP_RATED to 2] = page

            assertEquals(page, getMovies(MovieCategory.TOP_RATED, page = 2))
            assertEquals(listOf(MovieCategory.TOP_RATED to 2), movieRepository.movieRequests)
        }

    @Test
    fun `propagates the repository failure`() =
        runTest {
            movieRepository.failure = DataException.NoConnection()

            assertFailsWith<DataException.NoConnection> { getMovies(MovieCategory.TOP_RATED, page = 1) }
        }
}
