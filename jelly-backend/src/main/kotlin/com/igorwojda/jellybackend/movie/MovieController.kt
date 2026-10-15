package com.igorwojda.jellybackend.movie

import com.igorwojda.jellybackend.tmdb.TMDB_MAX_PAGE
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies")
class MovieController(
	private val movieService: MovieService,
) {
	@GetMapping
	@Operation(summary = "List movies by category (20 per page)")
	@ApiResponse(responseCode = "200", description = "Page of movies")
	@ApiResponse(responseCode = "400", description = "Unknown category or page out of range")
	@ApiResponse(responseCode = "502", description = "TMDB is unavailable")
	fun getMovies(
		@Parameter(schema = Schema(type = "string", allowableValues = ["upcoming", "top_rated", "now_playing"]))
		@RequestParam
		category: MovieCategory,
		@RequestParam(defaultValue = "1") @Min(1) @Max(TMDB_MAX_PAGE) page: Int,
	): MoviePageResponse = movieService.getMovies(category, page)

	@GetMapping("/{movieId}")
	@Operation(summary = "Get movie details")
	@ApiResponse(responseCode = "200", description = "Movie details")
	@ApiResponse(responseCode = "400", description = "Invalid movie ID")
	@ApiResponse(responseCode = "404", description = "Unknown movie")
	@ApiResponse(responseCode = "502", description = "TMDB is unavailable")
	fun getMovieDetails(
		@PathVariable @Positive movieId: Long,
	): MovieDetailsResponse = movieService.getMovieDetails(movieId)
}
