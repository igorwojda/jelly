package com.igorwojda.jellybackend.movie

import com.igorwojda.jellybackend.tmdb.TmdbUnavailableException
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(MovieController::class)
class MovieControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	private lateinit var movieService: MovieService

	@Test
	fun `list returns movies for category`() {
		given(movieService.getMovies(MovieCategory.TOP_RATED, 2)).willReturn(
			MoviePageResponse(2, 10, listOf(MovieSummaryResponse(238, "The Godfather", "https://img/p.jpg"))),
		)

		mockMvc.get("/api/v1/movies?category=top_rated&page=2").andExpect {
			status { isOk() }
			jsonPath("$.page") { value(2) }
			jsonPath("$.totalPages") { value(10) }
			jsonPath("$.results[0].id") { value(238) }
			jsonPath("$.results[0].title") { value("The Godfather") }
			jsonPath("$.results[0].posterUrl") { value("https://img/p.jpg") }
		}
	}

	@Test
	fun `list returns 400 for unknown category`() {
		mockMvc.get("/api/v1/movies?category=popular").andExpect { status { isBadRequest() } }
	}

	@Test
	fun `list returns 400 when category is missing`() {
		mockMvc.get("/api/v1/movies").andExpect { status { isBadRequest() } }
	}

	@Test
	fun `list returns 400 for page out of range`() {
		mockMvc.get("/api/v1/movies?category=upcoming&page=0").andExpect { status { isBadRequest() } }
		mockMvc.get("/api/v1/movies?category=upcoming&page=501").andExpect { status { isBadRequest() } }
	}

	@Test
	fun `details returns movie`() {
		given(movieService.getMovieDetails(238)).willReturn(
			MovieDetailsResponse(238, "The Godfather", null, "Overview", 1972, 8.7),
		)

		mockMvc.get("/api/v1/movies/238").andExpect {
			status { isOk() }
			jsonPath("$.releaseYear") { value(1972) }
			jsonPath("$.rating") { value(8.7) }
			jsonPath("$.overview") { value("Overview") }
		}
	}

	@Test
	fun `details returns 404 for unknown movie`() {
		given(movieService.getMovieDetails(1)).willThrow(MovieNotFoundException(1))

		mockMvc.get("/api/v1/movies/1").andExpect { status { isNotFound() } }
	}

	@Test
	fun `details returns 400 for invalid id`() {
		mockMvc.get("/api/v1/movies/abc").andExpect { status { isBadRequest() } }
		mockMvc.get("/api/v1/movies/-1").andExpect { status { isBadRequest() } }
	}

	@Test
	fun `details returns 502 when TMDB is unavailable`() {
		given(movieService.getMovieDetails(1)).willThrow(TmdbUnavailableException(RuntimeException()))

		mockMvc.get("/api/v1/movies/1").andExpect { status { isBadGateway() } }
	}
}
