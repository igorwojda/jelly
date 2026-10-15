package com.igorwojda.jellymobile.feature.movie.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.movie.data.model.backend.MovieDetailsResponseModel
import com.igorwojda.jellymobile.feature.movie.data.model.backend.MoviePageResponseModel
import com.igorwojda.jellymobile.feature.movie.data.model.backend.toDomain
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Movies from the Jelly backend (`/movies`), which proxies TMDB. */
internal class BackendMovieRemoteDataSource(
    private val httpClient: HttpClient,
) : MovieRemoteDataSource {
    override val source = RemoteSource.BACKEND

    override suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage =
        httpClient
            .get("movies") {
                parameter("category", category.apiValue)
                parameter("page", page)
            }.body<MoviePageResponseModel>()
            .toDomain()

    override suspend fun getMovieDetails(movieId: Long): MovieDetails =
        httpClient
            .get("movies/$movieId")
            .body<MovieDetailsResponseModel>()
            .toDomain()
}

/** Category value shared by the backend and TMDB (`/movie/{category}`) APIs. */
internal val MovieCategory.apiValue: String
    get() =
        when (this) {
            MovieCategory.NOW_PLAYING -> "now_playing"
            MovieCategory.UPCOMING -> "upcoming"
            MovieCategory.TOP_RATED -> "top_rated"
        }
