package com.igorwojda.jellymobile.feature.movie.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.jsonHttpClient
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TmdbMovieRemoteDataSourceTest {
    private val requests = mutableListOf<HttpRequestData>()

    @Test
    fun `maps a TMDB page like the backend does`() =
        runTest {
            val dataSource =
                dataSource(
                    """
                    {
                      "page": 2,
                      "total_pages": 12000,
                      "results": [
                        { "id": 1, "title": "Dune", "poster_path": "/dune.jpg", "vote_average": 8.1 },
                        { "id": 2, "title": "No Poster", "poster_path": null }
                      ]
                    }
                    """,
                )

            val page = dataSource.getMovies(MovieCategory.TOP_RATED, page = 2)

            assertEquals("/3/movie/top_rated", requests.single().url.encodedPath)
            assertEquals("Bearer token", requests.single().headers[HttpHeaders.Authorization])
            assertEquals("2", requests.single().url.parameters["page"])
            assertEquals(2, page.page)
            // TMDB rejects pages above 500.
            assertEquals(500, page.totalPages)
            assertEquals(
                listOf(
                    Movie(id = 1, title = "Dune", posterUrl = "https://image.tmdb.org/t/p/w342/dune.jpg"),
                    Movie(id = 2, title = "No Poster", posterUrl = null),
                ),
                page.movies,
            )
        }

    @Test
    fun `maps TMDB movie details like the backend does`() =
        runTest {
            val dataSource =
                dataSource(
                    """
                    {
                      "id": 7,
                      "title": "Dune",
                      "poster_path": "/dune.jpg",
                      "overview": "Spice.",
                      "release_date": "2021-09-15",
                      "vote_average": 7.847
                    }
                    """,
                )

            val details = dataSource.getMovieDetails(movieId = 7)

            assertEquals("/3/movie/7", requests.single().url.encodedPath)
            assertEquals(
                MovieDetails(
                    id = 7,
                    title = "Dune",
                    posterUrl = "https://image.tmdb.org/t/p/w780/dune.jpg",
                    overview = "Spice.",
                    releaseYear = 2021,
                    rating = 7.8,
                ),
                details,
            )
        }

    @Test
    fun `maps unknown TMDB details to empty values`() =
        runTest {
            val dataSource = dataSource("""{ "id": 7, "title": "Untitled", "overview": null, "release_date": "" }""")

            val details = dataSource.getMovieDetails(movieId = 7)

            assertEquals(MovieDetails(7, "Untitled", posterUrl = null, overview = "", releaseYear = null, rating = 0.0), details)
        }

    private fun dataSource(responseJson: String) =
        TmdbMovieRemoteDataSource(
            jsonHttpClient(
                baseUrl = "https://api.themoviedb.org/3/",
                bearerToken = "token",
                engine = MockEngine { request -> respondJson(request, responseJson) },
            ),
        )

    private fun MockRequestHandleScope.respondJson(
        request: HttpRequestData,
        json: String,
    ) = respond(
        content = json.also { requests += request },
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
}
