package com.igorwojda.jellymobile.feature.favorites.data.model

import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import kotlinx.serialization.Serializable

@Serializable
internal data class FavoriteMovieResponseModel(
    val id: Long,
    val title: String,
    val posterUrl: String? = null,
)

internal fun FavoriteMovieResponseModel.toDomain() = FavoriteMovie(id = id, title = title, posterUrl = posterUrl)
