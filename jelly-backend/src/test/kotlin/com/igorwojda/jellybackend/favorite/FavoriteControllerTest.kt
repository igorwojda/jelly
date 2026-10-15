package com.igorwojda.jellybackend.favorite

import com.igorwojda.jellybackend.movie.MovieNotFoundException
import com.igorwojda.jellybackend.movie.MovieSummaryResponse
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@WebMvcTest(FavoriteController::class)
class FavoriteControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	private lateinit var favoriteService: FavoriteService

	private val movie = MovieSummaryResponse(238, "The Godfather", "https://img/p.jpg")

	@Test
	fun `list returns favorites`() {
		given(favoriteService.getFavorites("user-1")).willReturn(listOf(movie))

		mockMvc.get("/api/v1/users/user-1/favorites").andExpect {
			status { isOk() }
			jsonPath("$[0].id") { value(238) }
			jsonPath("$[0].title") { value("The Godfather") }
		}
	}

	@Test
	fun `add returns 201 when created and 200 when already present`() {
		given(favoriteService.addFavorite("user-1", 238)).willReturn(AddFavoriteResult(movie, created = true))
		mockMvc.put("/api/v1/users/user-1/favorites/238").andExpect {
			status { isCreated() }
			jsonPath("$.id") { value(238) }
		}

		given(favoriteService.addFavorite("user-1", 238)).willReturn(AddFavoriteResult(movie, created = false))
		mockMvc.put("/api/v1/users/user-1/favorites/238").andExpect { status { isOk() } }
	}

	@Test
	fun `add returns 404 for unknown movie`() {
		given(favoriteService.addFavorite("user-1", 1)).willThrow(MovieNotFoundException(1))

		mockMvc.put("/api/v1/users/user-1/favorites/1").andExpect { status { isNotFound() } }
	}

	@Test
	fun `remove returns 204 and 404 when not a favorite`() {
		mockMvc.delete("/api/v1/users/user-1/favorites/238").andExpect { status { isNoContent() } }

		willThrow(FavoriteNotFoundException(1)).given(favoriteService).removeFavorite("user-1", 1)
		mockMvc.delete("/api/v1/users/user-1/favorites/1").andExpect { status { isNotFound() } }
	}

	@Test
	fun `returns 400 for invalid user id`() {
		mockMvc.get("/api/v1/users/bad user!/favorites").andExpect { status { isBadRequest() } }
		mockMvc.put("/api/v1/users/${"a".repeat(65)}/favorites/238").andExpect { status { isBadRequest() } }
	}
}
