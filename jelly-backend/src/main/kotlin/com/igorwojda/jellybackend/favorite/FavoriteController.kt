package com.igorwojda.jellybackend.favorite

import com.igorwojda.jellybackend.movie.MovieSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private const val USER_ID_PATTERN = "^[A-Za-z0-9_-]{1,64}$"

@RestController
@RequestMapping("/api/v1/users/{userId}/favorites")
@Tag(name = "Favorites")
class FavoriteController(
	private val favoriteService: FavoriteService,
) {
	@GetMapping
	@Operation(summary = "List favorite movies, most recently added first")
	@ApiResponse(responseCode = "200", description = "Favorite movies, empty for an unknown user")
	@ApiResponse(responseCode = "400", description = "Invalid user ID")
	fun getFavorites(
		@Parameter(description = "Client-generated user identifier, e.g. an install UUID")
		@PathVariable
		@Pattern(regexp = USER_ID_PATTERN)
		userId: String,
	): List<MovieSummaryResponse> = favoriteService.getFavorites(userId)

	@PutMapping("/{movieId}")
	@Operation(summary = "Add a movie to favorites (idempotent)")
	@ApiResponse(responseCode = "201", description = "Favorite added")
	@ApiResponse(responseCode = "200", description = "Movie was already a favorite")
	@ApiResponse(responseCode = "400", description = "Invalid user or movie ID")
	@ApiResponse(responseCode = "404", description = "Unknown movie")
	@ApiResponse(responseCode = "502", description = "TMDB is unavailable")
	fun addFavorite(
		@PathVariable @Pattern(regexp = USER_ID_PATTERN) userId: String,
		@PathVariable @Positive movieId: Long,
	): ResponseEntity<MovieSummaryResponse> {
		val result = favoriteService.addFavorite(userId, movieId)
		val status = if (result.created) HttpStatus.CREATED else HttpStatus.OK
		return ResponseEntity.status(status).body(result.movie)
	}

	@DeleteMapping("/{movieId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove a movie from favorites")
	@ApiResponse(responseCode = "204", description = "Favorite removed")
	@ApiResponse(responseCode = "400", description = "Invalid user or movie ID")
	@ApiResponse(responseCode = "404", description = "Movie is not a favorite")
	fun removeFavorite(
		@PathVariable @Pattern(regexp = USER_ID_PATTERN) userId: String,
		@PathVariable @Positive movieId: Long,
	) = favoriteService.removeFavorite(userId, movieId)
}
