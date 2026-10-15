package com.igorwojda.jellymobile.feature.movie.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.jsonHttpClient
import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BackendMovieRemoteDataSourceTest {
    private val requests = mutableListOf<HttpRequestData>()

    @Test
    fun `maps a backend page`() =
        runTest {
            val dataSource =
                dataSource(
                    """
                    {
                      "page": 2,
                      "totalPages": 12,
                      "results": [
                        { "id": 1, "title": "Dune", "posterUrl": "https://image.tmdb.org/t/p/w342/dune.jpg" },
                        { "id": 2, "title": "No Poster" }
                      ]
                    }
                    """,
                )

            val page = dataSource.getMovies(MovieCategory.UPCOMING, page = 2)

            assertEquals("/movies", requests.single().url.encodedPath)
            assertEquals("upcoming", requests.single().url.parameters["category"])
            assertEquals("2", requests.single().url.parameters["page"])
            // The backend has no token; it holds the TMDB one.
            assertNull(requests.single().headers[HttpHeaders.Authorization])
            assertEquals(2, page.page)
            assertEquals(12, page.totalPages)
            assertEquals(
                listOf(
                    Movie(id = 1, title = "Dune", posterUrl = "https://image.tmdb.org/t/p/w342/dune.jpg"),
                    Movie(id = 2, title = "No Poster", posterUrl = null),
                ),
                page.movies,
            )
        }

    @Test
    fun `maps backend movie details`() =
        runTest {
            val dataSource =
                dataSource(
                    """
                    {
                      "id": 7,
                      "title": "Dune",
                      "posterUrl": "https://image.tmdb.org/t/p/w780/dune.jpg",
                      "overview": "Spice.",
                      "releaseYear": 2021,
                      "rating": 7.8,
                      "unknownField": true
                    }
                    """,
                )

            val details = dataSource.getMovieDetails(movieId = 7)

            assertEquals("/movies/7", requests.single().url.encodedPath)
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
    fun `throws when the movie doesn't exist`() =
        runTest {
            val dataSource = BackendMovieRemoteDataSource(jsonHttpClient(BASE_URL, engine = MockEngine { respondError(HttpStatusCode.NotFound) }))

            val failure = assertFailsWith<ClientRequestException> { dataSource.getMovieDetails(movieId = 7) }

            assertEquals(HttpStatusCode.NotFound, failure.response.status)
        }

    @Test
    fun `sends every category in the API format`() {
        assertEquals(
            listOf("now_playing", "upcoming", "top_rated"),
            MovieCategory.entries.map { it.apiValue },
        )
    }

    private fun dataSource(responseJson: String) =
        BackendMovieRemoteDataSource(
            jsonHttpClient(baseUrl = BASE_URL, engine = MockEngine { request -> respondJson(request, responseJson) }),
        )

    private fun MockRequestHandleScope.respondJson(
        request: HttpRequestData,
        json: String,
    ) = respond(
        content = json.also { requests += request },
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private companion object {
        const val BASE_URL = "http://localhost:8080/"
    }
}
