package com.igorwojda.jellymobile.feature.movie.data.database

import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import kotlin.test.Test
import kotlin.test.assertEquals

class MovieEntitiesTest {
    @Test
    fun `maps a movie to an entity and back`() {
        val movie = Movie(id = 1, title = "Dune", posterUrl = "https://image.tmdb.org/t/p/w342/dune.jpg")

        val entity = movie.toEntity()

        assertEquals(MovieEntity(id = 1, title = "Dune", posterUrl = "https://image.tmdb.org/t/p/w342/dune.jpg"), entity)
        assertEquals(movie, entity.toDomain())
    }

    @Test
    fun `maps movie details to an entity and back`() {
        val details = MovieDetails(id = 7, title = "Dune", posterUrl = null, overview = "Spice.", releaseYear = null, rating = 7.8)

        val entity = details.toEntity()

        assertEquals(
            MovieDetailsEntity(id = 7, title = "Dune", posterUrl = null, overview = "Spice.", releaseYear = null, rating = 7.8),
            entity,
        )
        assertEquals(details, entity.toDomain())
    }
}
