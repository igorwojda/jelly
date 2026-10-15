package com.igorwojda.jellybackend.movie

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Movie as shown in lists (movie list and favorites)")
data class MovieSummaryResponse(
	val id: Long,
	val title: String,
	@Schema(description = "Absolute poster URL, null when the movie has no poster")
	val posterUrl: String?,
)

@Schema(description = "One page of movies. Load the next page while `page < totalPages`.")
data class MoviePageResponse(
	val page: Int,
	val totalPages: Int,
	val results: List<MovieSummaryResponse>,
)

data class MovieDetailsResponse(
	val id: Long,
	val title: String,
	@Schema(description = "Absolute poster URL, null when the movie has no poster")
	val posterUrl: String?,
	val overview: String,
	@Schema(description = "Null when the release date is unknown")
	val releaseYear: Int?,
	@Schema(description = "Average user rating from 0 to 10, rounded to one decimal place")
	val rating: Double,
)
