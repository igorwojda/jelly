package com.igorwojda.jellymobile.feature.movie.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory

@Dao
internal interface MovieDao {
    @Query("SELECT * FROM category_page WHERE category = :category AND page = :page")
    suspend fun getPage(
        category: MovieCategory,
        page: Int,
    ): CategoryPageEntity?

    @Query(
        """
        SELECT movie.* FROM category_page_movie
        INNER JOIN movie ON movie.id = category_page_movie.movieId
        WHERE category_page_movie.category = :category AND category_page_movie.page = :page
        ORDER BY category_page_movie.position
        """,
    )
    suspend fun getPageMovies(
        category: MovieCategory,
        page: Int,
    ): List<MovieEntity>

    /**
     * Stores [page] with its [movies], dropping it and every later page of the category first: the cached pages stay
     * one consistent run from page 1, rather than a mix of pages fetched at different times.
     */
    @Transaction
    suspend fun replacePage(
        page: CategoryPageEntity,
        movies: List<MovieEntity>,
    ) {
        deletePagesFrom(page.category, page.page)
        upsertMovies(movies)
        insertPage(page)
        insertPageMovies(
            movies.mapIndexed { position, movie ->
                CategoryPageMovieEntity(category = page.category, page = page.page, position = position, movieId = movie.id)
            },
        )
    }

    @Query("DELETE FROM category_page WHERE category = :category AND page >= :fromPage")
    suspend fun deletePagesFrom(
        category: MovieCategory,
        fromPage: Int,
    )

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert
    suspend fun insertPage(page: CategoryPageEntity)

    @Insert
    suspend fun insertPageMovies(movies: List<CategoryPageMovieEntity>)

    @Query("SELECT * FROM movie_details WHERE id = :movieId")
    suspend fun getDetails(movieId: Long): MovieDetailsEntity?

    @Upsert
    suspend fun upsertDetails(details: MovieDetailsEntity)
}
