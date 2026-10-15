package com.igorwojda.jellybackend.movie

import com.igorwojda.jellybackend.api.NotFoundException
import com.igorwojda.jellybackend.tmdb.TmdbClient
import org.springframework.stereotype.Service

class MovieNotFoundException(
	movieId: Long,
) : NotFoundException("Movie $movieId not found")

@Service
class MovieService(
	private val tmdbClient: TmdbClient,
	private val movieMapper: MovieMapper,
) {
	fun getMovies(
		category: MovieCategory,
		page: Int,
	): MoviePageResponse = movieMapper.toPage(tmdbClient.getMovies(category, page))

	fun getMovieDetails(movieId: Long): MovieDetailsResponse = movieMapper.toDetails(tmdbClient.getMovieDetails(movieId))
}
