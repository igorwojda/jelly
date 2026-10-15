package com.igorwojda.jellybackend.favorite

import com.igorwojda.jellybackend.api.NotFoundException
import com.igorwojda.jellybackend.movie.MovieMapper
import com.igorwojda.jellybackend.movie.MovieSummaryResponse
import com.igorwojda.jellybackend.tmdb.TmdbClient
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

class FavoriteNotFoundException(
	movieId: Long,
) : NotFoundException("Movie $movieId is not in favorites")

data class AddFavoriteResult(
	val movie: MovieSummaryResponse,
	val created: Boolean,
)

@Service
class FavoriteService(
	private val favoriteMovieRepository: FavoriteMovieRepository,
	private val tmdbClient: TmdbClient,
	private val movieMapper: MovieMapper,
) {
	fun getFavorites(userId: String): List<MovieSummaryResponse> =
		favoriteMovieRepository.findAllByUserIdOrderByAddedAtDesc(userId).map { it.toSummary() }

	/** Idempotent: adding an existing favorite returns it with `created = false`. */
	fun addFavorite(
		userId: String,
		movieId: Long,
	): AddFavoriteResult {
		favoriteMovieRepository.findByUserIdAndMovieId(userId, movieId)?.let {
			return AddFavoriteResult(it.toSummary(), created = false)
		}

		val movie = tmdbClient.getMovieDetails(movieId)
		return try {
			val favorite = favoriteMovieRepository.saveAndFlush(FavoriteMovie(userId, movieId, movie.title, movie.posterPath))
			AddFavoriteResult(favorite.toSummary(), created = true)
		} catch (exception: DataIntegrityViolationException) {
			// A concurrent request added the same favorite between the lookup and the insert
			val existing = favoriteMovieRepository.findByUserIdAndMovieId(userId, movieId) ?: throw exception
			AddFavoriteResult(existing.toSummary(), created = false)
		}
	}

	@Transactional
	fun removeFavorite(
		userId: String,
		movieId: Long,
	) {
		if (favoriteMovieRepository.deleteByUserIdAndMovieId(userId, movieId) == 0L) {
			throw FavoriteNotFoundException(movieId)
		}
	}

	private fun FavoriteMovie.toSummary() = movieMapper.toSummary(movieId, title, posterPath)
}
