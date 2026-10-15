package com.igorwojda.jellybackend.movie

import com.igorwojda.jellybackend.tmdb.TmdbMovie
import com.igorwojda.jellybackend.tmdb.TmdbMovieDetails
import com.igorwojda.jellybackend.tmdb.TmdbMoviePage
import com.igorwojda.jellybackend.tmdb.TmdbProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MovieMapperTest {
	private val mapper = MovieMapper(TmdbProperties("token", "https://tmdb.test", "https://img.test/t/p/"))

	@Test
	fun `toPage builds poster urls and caps total pages at TMDB limit`() {
		val page = TmdbMoviePage(1, 567, listOf(TmdbMovie(1, "A", "/a.jpg"), TmdbMovie(2, "B", null)))

		assertThat(mapper.toPage(page)).isEqualTo(
			MoviePageResponse(
				page = 1,
				totalPages = 500,
				results =
					listOf(
						MovieSummaryResponse(1, "A", "https://img.test/t/p/w342/a.jpg"),
						MovieSummaryResponse(2, "B", null),
					),
			),
		)
	}

	@Test
	fun `toDetails extracts release year and rounds rating`() {
		val details = TmdbMovieDetails(238, "The Godfather", "/g.jpg", "Overview", "1972-03-14", 8.687)

		assertThat(mapper.toDetails(details)).isEqualTo(
			MovieDetailsResponse(238, "The Godfather", "https://img.test/t/p/w780/g.jpg", "Overview", 1972, 8.7),
		)
	}

	@Test
	fun `toDetails handles missing optional fields`() {
		val details = TmdbMovieDetails(1, "Unknown", "", null, "", 0.0)

		assertThat(mapper.toDetails(details)).isEqualTo(MovieDetailsResponse(1, "Unknown", null, "", null, 0.0))
	}
}
