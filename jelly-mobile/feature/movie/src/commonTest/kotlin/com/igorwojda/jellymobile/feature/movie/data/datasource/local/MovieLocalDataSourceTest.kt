package com.igorwojda.jellymobile.feature.movie.data.datasource.local

import com.igorwojda.jellymobile.feature.movie.data.database.FakeMovieDao
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MovieLocalDataSourceTest {
    private val localDataSource = MovieLocalDataSource(FakeMovieDao())

    @Test
    fun `returns nothing for a page that isn't cached`() =
        runTest {
            assertNull(localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1))
        }

    @Test
    fun `returns a cached page with the movies in their original order`() =
        runTest {
            val page = MoviePage(page = 1, totalPages = 3, movies = listOf(dune, alien, heat))

            localDataSource.saveMovies(MovieCategory.NOW_PLAYING, page)

            assertEquals(page, localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1))
            assertNull(localDataSource.getMovies(MovieCategory.TOP_RATED, page = 1))
        }

    @Test
    fun `returns a cached empty page`() =
        runTest {
            val page = MoviePage(page = 1, totalPages = 1, movies = emptyList())

            localDataSource.saveMovies(MovieCategory.UPCOMING, page)

            assertEquals(page, localDataSource.getMovies(MovieCategory.UPCOMING, page = 1))
        }

    @Test
    fun `keeps the first of a movie listed twice on one page`() =
        runTest {
            localDataSource.saveMovies(MovieCategory.NOW_PLAYING, MoviePage(page = 1, totalPages = 1, movies = listOf(dune, alien, dune)))

            assertEquals(listOf(dune, alien), localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1)?.movies)
        }

    @Test
    fun `drops the later pages of a category when a page is cached again`() =
        runTest {
            localDataSource.saveMovies(MovieCategory.NOW_PLAYING, MoviePage(page = 1, totalPages = 2, movies = listOf(dune)))
            localDataSource.saveMovies(MovieCategory.NOW_PLAYING, MoviePage(page = 2, totalPages = 2, movies = listOf(alien)))
            localDataSource.saveMovies(MovieCategory.TOP_RATED, MoviePage(page = 2, totalPages = 2, movies = listOf(heat)))

            localDataSource.saveMovies(MovieCategory.NOW_PLAYING, MoviePage(page = 1, totalPages = 2, movies = listOf(heat)))

            assertEquals(listOf(heat), localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1)?.movies)
            assertNull(localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 2))
            // Other categories are untouched.
            assertEquals(listOf(heat), localDataSource.getMovies(MovieCategory.TOP_RATED, page = 2)?.movies)
        }

    @Test
    fun `returns cached movie details`() =
        runTest {
            val details = MovieDetails(id = 7, title = "Dune", posterUrl = null, overview = "Spice.", releaseYear = 2021, rating = 7.8)

            localDataSource.saveMovieDetails(details)

            assertEquals(details, localDataSource.getMovieDetails(movieId = 7))
            assertNull(localDataSource.getMovieDetails(movieId = 8))
        }

    private companion object {
        val dune = Movie(id = 1, title = "Dune", posterUrl = null)
        val alien = Movie(id = 2, title = "Alien", posterUrl = null)
        val heat = Movie(id = 3, title = "Heat", posterUrl = null)
    }
}
