package com.igorwojda.jellybackend.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

/** TMDB rejects pages above 500 even when `total_pages` is higher. */
const val TMDB_MAX_PAGE = 500L

data class TmdbMoviePage(
	val page: Int,
	@JsonProperty("total_pages") val totalPages: Int,
	val results: List<TmdbMovie>,
)

data class TmdbMovie(
	val id: Long,
	val title: String,
	@JsonProperty("poster_path") val posterPath: String?,
)

data class TmdbMovieDetails(
	val id: Long,
	val title: String,
	@JsonProperty("poster_path") val posterPath: String?,
	val overview: String?,
	@JsonProperty("release_date") val releaseDate: String?,
	@JsonProperty("vote_average") val voteAverage: Double,
)
