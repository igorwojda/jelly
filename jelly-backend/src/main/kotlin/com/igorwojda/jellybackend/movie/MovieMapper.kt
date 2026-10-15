package com.igorwojda.jellybackend.movie

import com.igorwojda.jellybackend.tmdb.TMDB_MAX_PAGE
import com.igorwojda.jellybackend.tmdb.TmdbMovieDetails
import com.igorwojda.jellybackend.tmdb.TmdbMoviePage
import com.igorwojda.jellybackend.tmdb.TmdbProperties
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class MovieMapper(
	properties: TmdbProperties,
) {
	private val imageBaseUrl = properties.imageBaseUrl.trimEnd('/')

	fun toPage(page: TmdbMoviePage): MoviePageResponse =
		MoviePageResponse(
			page = page.page,
			totalPages = minOf(page.totalPages, TMDB_MAX_PAGE.toInt()),
			results = page.results.map { toSummary(it.id, it.title, it.posterPath) },
		)

	fun toSummary(
		id: Long,
		title: String,
		posterPath: String?,
	): MovieSummaryResponse = MovieSummaryResponse(id, title, posterUrl(posterPath, LIST_POSTER_SIZE))

	fun toDetails(details: TmdbMovieDetails): MovieDetailsResponse =
		MovieDetailsResponse(
			id = details.id,
			title = details.title,
			posterUrl = posterUrl(details.posterPath, DETAILS_POSTER_SIZE),
			overview = details.overview.orEmpty(),
			releaseYear = details.releaseDate?.take(4)?.toIntOrNull(),
			rating = (details.voteAverage * 10).roundToInt() / 10.0,
		)

	private fun posterUrl(
		posterPath: String?,
		size: String,
	): String? = posterPath?.takeIf { it.isNotBlank() }?.let { "$imageBaseUrl/$size$it" }

	private companion object {
		const val LIST_POSTER_SIZE = "w342"
		const val DETAILS_POSTER_SIZE = "w780"
	}
}
