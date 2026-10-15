package com.igorwojda.jellymobile.feature.movie.data.model.backend

import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import kotlin.test.Test
import kotlin.test.assertEquals

class BackendResponseModelsTest {
    @Test
    fun `maps a movie page`() {
        val response =
            MoviePageResponseModel(
                page = 2,
                totalPages = 10,
                results =
                    listOf(
                        MovieSummaryResponseModel(id = 1, title = "Dune", posterUrl = "https://example.com/dune.jpg"),
                        MovieSummaryResponseModel(id = 2, title = "No Poster"),
                    ),
            )

        assertEquals(
            MoviePage(
                page = 2,
                totalPages = 10,
                movies =
                    listOf(
                        Movie(id = 1, title = "Dune", posterUrl = "https://example.com/dune.jpg"),
                        Movie(id = 2, title = "No Poster", posterUrl = null),
                    ),
            ),
            response.toDomain(),
        )
    }

    @Test
    fun `maps an empty movie page`() {
        val response = MoviePageResponseModel(page = 1, totalPages = 1, results = emptyList())

        assertEquals(MoviePage(page = 1, totalPages = 1, movies = emptyList()), response.toDomain())
    }

    @Test
    fun `maps movie details`() {
        val response =
            MovieDetailsResponseModel(
                id = 7,
                title = "Dune",
                posterUrl = "https://example.com/dune.jpg",
                overview = "Spice.",
                releaseYear = 2021,
                rating = 7.8,
            )

        assertEquals(
            MovieDetails(
                id = 7,
                title = "Dune",
                posterUrl = "https://example.com/dune.jpg",
                overview = "Spice.",
                releaseYear = 2021,
                rating = 7.8,
            ),
            response.toDomain(),
        )
    }

    @Test
    fun `maps movie details with unknown values`() {
        val response = MovieDetailsResponseModel(id = 7, title = "Untitled", overview = "", rating = 0.0)

        assertEquals(
            MovieDetails(id = 7, title = "Untitled", posterUrl = null, overview = "", releaseYear = null, rating = 0.0),
            response.toDomain(),
        )
    }
}
