package com.igorwojda.jellybackend.tmdb

import com.igorwojda.jellybackend.movie.MovieCategory
import com.igorwojda.jellybackend.movie.MovieNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

// One client per class: the auto-configured MockRestServiceServer can be bound to a single RestClient only
@RestClientTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TmdbClientTest(
	@Autowired restClientBuilder: RestClient.Builder,
	@Autowired private val server: MockRestServiceServer,
) {
	private val client = TmdbClient(restClientBuilder, TmdbProperties("token", BASE_URL, "https://img.test"))

	@AfterEach
	fun resetServer() = server.reset()

	@Test
	fun `getMovies requests category page with bearer token and parses response`() {
		server
			.expect(requestTo("$BASE_URL/movie/top_rated?page=2"))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
			.andRespond(
				withSuccess(
					"""{"page":2,"total_pages":600,"total_results":12000,
					"results":[{"id":238,"title":"The Godfather","poster_path":"/p.jpg","adult":false}]}""",
					MediaType.APPLICATION_JSON,
				),
			)

		val page = client.getMovies(MovieCategory.TOP_RATED, 2)

		assertThat(page).isEqualTo(TmdbMoviePage(2, 600, listOf(TmdbMovie(238, "The Godfather", "/p.jpg"))))
	}

	@Test
	fun `getMovieDetails maps 404 to MovieNotFoundException`() {
		server.expect(requestTo("$BASE_URL/movie/1")).andRespond(withStatus(HttpStatus.NOT_FOUND))

		assertThatThrownBy { client.getMovieDetails(1) }.isInstanceOf(MovieNotFoundException::class.java)
	}

	@Test
	fun `getMovieDetails maps server error to TmdbUnavailableException`() {
		server.expect(requestTo("$BASE_URL/movie/1")).andRespond(withServerError())

		assertThatThrownBy { client.getMovieDetails(1) }.isInstanceOf(TmdbUnavailableException::class.java)
	}

	private companion object {
		const val BASE_URL = "https://tmdb.test/3"
	}
}
