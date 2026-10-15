package com.igorwojda.jellybackend.favorite

import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteMovieRepository : JpaRepository<FavoriteMovie, Long> {
	fun findAllByUserIdOrderByAddedAtDesc(userId: String): List<FavoriteMovie>

	fun findByUserIdAndMovieId(
		userId: String,
		movieId: Long,
	): FavoriteMovie?

	fun deleteByUserIdAndMovieId(
		userId: String,
		movieId: Long,
	): Long
}
