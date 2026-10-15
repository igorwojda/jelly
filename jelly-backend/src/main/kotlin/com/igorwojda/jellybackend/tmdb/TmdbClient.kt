package com.igorwojda.jellybackend.tmdb

import com.igorwojda.jellybackend.movie.MovieCategory
import com.igorwojda.jellybackend.movie.MovieNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body

class TmdbUnavailableException(cause: Throwable) : RuntimeException("TMDB request failed", cause)

@Component
class TmdbClient(
	restClientBuilder: RestClient.Builder,
	properties: TmdbProperties,
) {
	private val restClient =
		restClientBuilder
			.baseUrl(properties.baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.apiToken}")
			.build()

	fun getMovies(
		category: MovieCategory,
		page: Int,
	): TmdbMoviePage =
		callTmdb {
			restClient
				.get()
				.uri("/movie/{category}?page={page}", category.tmdbPath, page)
				.retrieve()
				.body<TmdbMoviePage>()
		}

	fun getMovieDetails(movieId: Long): TmdbMovieDetails =
		callTmdb {
			restClient
				.get()
				.uri("/movie/{movieId}", movieId)
				.retrieve()
				.onStatus({ it == HttpStatus.NOT_FOUND }) { _, _ -> throw MovieNotFoundException(movieId) }
				.body<TmdbMovieDetails>()
		}

	private fun <T : Any> callTmdb(request: () -> T?): T =
		try {
			request() ?: throw TmdbUnavailableException(IllegalStateException("Empty TMDB response"))
		} catch (exception: RestClientException) {
			throw TmdbUnavailableException(exception)
		}

	private val MovieCategory.tmdbPath: String
		get() =
			when (this) {
				MovieCategory.UPCOMING -> "upcoming"
				MovieCategory.TOP_RATED -> "top_rated"
				MovieCategory.NOW_PLAYING -> "now_playing"
			}
}
