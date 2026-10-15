package com.igorwojda.jellymobile.feature.movie.data.model.backend

import com.igorwojda.jellymobile.feature.movie.domain.model.Movie
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieSummaryResponseModel(
    val id: Long,
    val title: String,
    val posterUrl: String? = null,
)

internal fun MovieSummaryResponseModel.toDomain() = Movie(id = id, title = title, posterUrl = posterUrl)
