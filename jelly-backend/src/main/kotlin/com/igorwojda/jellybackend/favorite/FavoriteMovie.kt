package com.igorwojda.jellybackend.favorite

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/** Stores a snapshot of the title and poster so listing favorites needs no TMDB calls. */
@Entity
@Table(name = "favorite_movies")
class FavoriteMovie(
	@Column(name = "user_id", nullable = false)
	val userId: String,
	@Column(name = "movie_id", nullable = false)
	val movieId: Long,
	@Column(name = "title", nullable = false)
	val title: String,
	@Column(name = "poster_path")
	val posterPath: String?,
	@Column(name = "added_at", nullable = false)
	val addedAt: Instant = Instant.now(),
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
