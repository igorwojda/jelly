package com.igorwojda.jellymobile.feature.movie.data.datasource.local

import com.igorwojda.jellymobile.feature.movie.data.database.CategoryPageEntity
import com.igorwojda.jellymobile.feature.movie.data.database.MovieDao
import com.igorwojda.jellymobile.feature.movie.data.database.toDomain
import com.igorwojda.jellymobile.feature.movie.data.database.toEntity
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage

/** Movies cached on the device (Room), so pages and details seen before stay available offline. */
internal class MovieLocalDataSource(
    private val movieDao: MovieDao,
) {
    /** The cached [page] of [category], `null` when it hasn't been cached. */
    suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage? {
        val cachedPage = movieDao.getPage(category, page) ?: return null
        return MoviePage(
            page = cachedPage.page,
            totalPages = cachedPage.totalPages,
            movies = movieDao.getPageMovies(category, page).map { it.toDomain() },
        )
    }

    /** Caches [moviePage]; later pages of the [category] are dropped (see [MovieDao.replacePage]). */
    suspend fun saveMovies(
        category: MovieCategory,
        moviePage: MoviePage,
    ) = movieDao.replacePage(
        page = CategoryPageEntity(category = category, page = moviePage.page, totalPages = moviePage.totalPages),
        // A movie listed twice on one page would break the (category, page, position) → movie mapping.
        movies = moviePage.movies.distinctBy { it.id }.map { it.toEntity() },
    )

    suspend fun getMovieDetails(movieId: Long): MovieDetails? = movieDao.getDetails(movieId)?.toDomain()

    suspend fun saveMovieDetails(details: MovieDetails) = movieDao.upsertDetails(details.toEntity())
}
