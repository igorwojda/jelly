package com.igorwojda.jellymobile.feature.movie.data.repository

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSourceHealthTracker
import com.igorwojda.jellymobile.feature.base.domain.model.DataException
import com.igorwojda.jellymobile.feature.movie.data.database.FakeMovieDao
import com.igorwojda.jellymobile.feature.movie.data.datasource.local.MovieLocalDataSource
import com.igorwojda.jellymobile.feature.movie.data.datasource.remote.MovieRemoteDataSource
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class MovieRepositoryImplTest {
    private val backend = FakeMovieRemoteDataSource(RemoteSource.BACKEND)
    private val tmdb = FakeMovieRemoteDataSource(RemoteSource.TMDB)
    private val localDataSource = MovieLocalDataSource(FakeMovieDao())
    private val repository =
        MovieRepositoryImpl(
            remoteDataSources = RemoteDataSourceChain(listOf(backend, tmdb), RemoteSourceHealthTracker()),
            localDataSource = localDataSource,
        )

    @Test
    fun `returns movies from the backend and caches them`() =
        runTest {
            backend.page = backendPage

            val page = repository.getMovies(MovieCategory.NOW_PLAYING, page = 1)

            assertEquals(backendPage, page)
            assertEquals(listOf(MovieCategory.NOW_PLAYING to 1), backend.movieRequests)
            assertEquals(backendPage, localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1))
        }

    @Test
    fun `returns movies from TMDB when the backend is unavailable`() =
        runTest {
            backend.failure = IOException("Connection refused")
            tmdb.page = tmdbPage

            val page = repository.getMovies(MovieCategory.NOW_PLAYING, page = 1)

            assertEquals(tmdbPage, page)
            assertEquals(tmdbPage, localDataSource.getMovies(MovieCategory.NOW_PLAYING, page = 1))
        }

    @Test
    fun `returns cached movies when no remote source is reachable`() =
        runTest {
            backend.page = backendPage
            repository.getMovies(MovieCategory.NOW_PLAYING, page = 1)
            backend.failure = IOException("No internet")
            tmdb.failure = IOException("No internet")

            assertEquals(backendPage, repository.getMovies(MovieCategory.NOW_PLAYING, page = 1))
        }

    @Test
    fun `throws when no remote source is reachable and nothing is cached`() =
        runTest {
            backend.failure = IOException("No internet")
            tmdb.failure = IOException("No internet")

            assertFailsWith<DataException.NoConnection> { repository.getMovies(MovieCategory.NOW_PLAYING, page = 1) }
        }

    @Test
    fun `returns movie details from the backend and caches them`() =
        runTest {
            backend.details = dune

            assertEquals(dune, repository.getMovieDetails(movieId = dune.id))
            assertEquals(listOf(dune.id), backend.detailsRequests)
            assertEquals(dune, localDataSource.getMovieDetails(dune.id))
        }

    @Test
    fun `returns cached movie details when no remote source is reachable`() =
        runTest {
            localDataSource.saveMovieDetails(dune)
            backend.failure = IOException("No internet")
            tmdb.failure = IOException("No internet")

            assertEquals(dune, repository.getMovieDetails(movieId = dune.id))
        }

    @Test
    fun `throws not found rather than returning stale cached details`() =
        runTest {
            localDataSource.saveMovieDetails(dune)
            backend.failure = clientError(HttpStatusCode.NotFound)
            tmdb.failure = clientError(HttpStatusCode.NotFound)

            assertFailsWith<DataException.NotFound> { repository.getMovieDetails(movieId = dune.id) }
        }

    private suspend fun clientError(status: HttpStatusCode): Exception {
        val httpClient = HttpClient(MockEngine { respondError(status) }) { expectSuccess = true }
        return assertFailsWith<ClientRequestException> { httpClient.get("https://example.com") }
    }

    private class FakeMovieRemoteDataSource(
        override val source: RemoteSource,
    ) : MovieRemoteDataSource {
        var failure: Exception? = null
        var page: MoviePage? = null
        var details: MovieDetails? = null
        val movieRequests = mutableListOf<Pair<MovieCategory, Int>>()
        val detailsRequests = mutableListOf<Long>()

        override suspend fun getMovies(
            category: MovieCategory,
            page: Int,
        ): MoviePage {
            movieRequests += category to page
            failure?.let { throw it }
            return checkNotNull(this.page)
        }

        override suspend fun getMovieDetails(movieId: Long): MovieDetails {
            detailsRequests += movieId
            failure?.let { throw it }
            return checkNotNull(details)
        }
    }

    private companion object {
        val backendPage = MoviePage(page = 1, totalPages = 5, movies = listOf(Movie(id = 1, title = "Dune", posterUrl = null)))
        val tmdbPage = MoviePage(page = 1, totalPages = 500, movies = listOf(Movie(id = 2, title = "Alien", posterUrl = null)))
        val dune = MovieDetails(id = 1, title = "Dune", posterUrl = null, overview = "Spice.", releaseYear = 2021, rating = 7.8)
    }
}
