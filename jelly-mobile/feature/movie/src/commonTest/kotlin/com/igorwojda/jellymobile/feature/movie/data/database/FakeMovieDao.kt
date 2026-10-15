package com.igorwojda.jellymobile.feature.movie.data.database

import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory

/** In-memory [MovieDao] with the same queries; the `@Transaction` [replacePage] runs the interface's default. */
internal class FakeMovieDao : MovieDao {
    private val movies = mutableMapOf<Long, MovieEntity>()
    private val pages = mutableMapOf<Pair<MovieCategory, Int>, CategoryPageEntity>()
    private val pageMovies = mutableListOf<CategoryPageMovieEntity>()
    private val details = mutableMapOf<Long, MovieDetailsEntity>()

    override suspend fun getPage(
        category: MovieCategory,
        page: Int,
    ) = pages[category to page]

    override suspend fun getPageMovies(
        category: MovieCategory,
        page: Int,
    ) = pageMovies
        .filter { it.category == category && it.page == page }
        .sortedBy { it.position }
        .map { movies.getValue(it.movieId) }

    override suspend fun deletePagesFrom(
        category: MovieCategory,
        fromPage: Int,
    ) {
        pages.keys.removeAll { (pageCategory, page) -> pageCategory == category && page >= fromPage }
        // ON DELETE CASCADE
        pageMovies.removeAll { it.category == category && it.page >= fromPage }
    }

    override suspend fun upsertMovies(movies: List<MovieEntity>) {
        movies.forEach { this.movies[it.id] = it }
    }

    override suspend fun insertPage(page: CategoryPageEntity) {
        check(pages.put(page.category to page.page, page) == null) { "Duplicate page $page" }
    }

    override suspend fun insertPageMovies(movies: List<CategoryPageMovieEntity>) {
        movies.forEach { movie ->
            check(pageMovies.none { it.category == movie.category && it.page == movie.page && it.position == movie.position }) {
                "Duplicate page movie $movie"
            }
        }
        pageMovies += movies
    }

    override suspend fun getDetails(movieId: Long) = details[movieId]

    override suspend fun upsertDetails(details: MovieDetailsEntity) {
        this.details[details.id] = details
    }
}
