package com.igorwojda.jellymobile.feature.movie.data.model.backend

import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import kotlinx.serialization.Serializable

@Serializable
internal data class MoviePageResponseModel(
    val page: Int,
    val totalPages: Int,
    val results: List<MovieSummaryResponseModel>,
)

internal fun MoviePageResponseModel.toDomain() =
    MoviePage(page = page, totalPages = totalPages, movies = results.map { it.toDomain() })
