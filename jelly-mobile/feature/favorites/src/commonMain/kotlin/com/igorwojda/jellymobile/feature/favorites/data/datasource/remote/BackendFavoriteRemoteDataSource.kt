package com.igorwojda.jellymobile.feature.favorites.data.datasource.remote

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteSource
import com.igorwojda.jellymobile.feature.favorites.data.datasource.local.UserIdLocalDataSource
import com.igorwojda.jellymobile.feature.favorites.data.model.FavoriteMovieResponseModel
import com.igorwojda.jellymobile.feature.favorites.data.model.toDomain
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode

/** Favorites stored by the Jelly backend (`/users/{userId}/favorites`). */
internal class BackendFavoriteRemoteDataSource(
    private val httpClient: HttpClient,
    private val userIdLocalDataSource: UserIdLocalDataSource,
) : FavoriteRemoteDataSource {
    override val source = RemoteSource.BACKEND

    private val favoritesPath get() = "users/${userIdLocalDataSource.userId}/favorites"

    override suspend fun getFavorites(): List<FavoriteMovie> =
        httpClient.get(favoritesPath).body<List<FavoriteMovieResponseModel>>().map { it.toDomain() }

    override suspend fun add(movieId: Long) {
        httpClient.put("$favoritesPath/$movieId")
    }

    override suspend fun remove(movieId: Long) {
        try {
            httpClient.delete("$favoritesPath/$movieId")
        } catch (e: ClientRequestException) {
            // 404: the movie is already gone from the backend, which is what the user asked for.
            if (e.response.status != HttpStatusCode.NotFound) throw e
        }
    }
}
