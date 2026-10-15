package com.igorwojda.jellymobile.feature.movie.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.movie.data.model.tmdb.TmdbMovieDetailsResponseModel
import com.igorwojda.jellymobile.feature.movie.data.model.tmdb.TmdbMoviePageResponseModel
import com.igorwojda.jellymobile.feature.movie.data.model.tmdb.toDomain
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Movies from the TMDB API called directly, the fallback when the Jelly backend is unavailable. */
internal class TmdbMovieRemoteDataSource(
    private val httpClient: HttpClient,
) : MovieRemoteDataSource {
    override val source = RemoteSource.TMDB

    override suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage =
        httpClient
            .get("movie/${category.apiValue}") {
                parameter("page", page)
            }.body<TmdbMoviePageResponseModel>()
            .toDomain()

    override suspend fun getMovieDetails(movieId: Long): MovieDetails =
        httpClient
            .get("movie/$movieId")
            .body<TmdbMovieDetailsResponseModel>()
            .toDomain()
}
