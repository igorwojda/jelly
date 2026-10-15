package com.igorwojda.jellybackend.favorite

import com.igorwojda.jellybackend.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
class FavoriteMovieRepositoryTest(
	@Autowired private val repository: FavoriteMovieRepository,
) {
	@Test
	fun `lists favorites of a user newest first`() {
		repository.save(FavoriteMovie("user-1", 1, "Old", null, Instant.parse("2026-01-01T00:00:00Z")))
		repository.save(FavoriteMovie("user-1", 2, "New", "/new.jpg", Instant.parse("2026-02-01T00:00:00Z")))
		repository.save(FavoriteMovie("user-2", 3, "Other user", null))

		val favorites = repository.findAllByUserIdOrderByAddedAtDesc("user-1")

		assertThat(favorites.map { it.movieId }).containsExactly(2L, 1L)
	}

	@Test
	fun `rejects duplicate favorite for the same user`() {
		repository.saveAndFlush(FavoriteMovie("user-1", 1, "Movie", null))

		assertThatThrownBy { repository.saveAndFlush(FavoriteMovie("user-1", 1, "Movie", null)) }
			.isInstanceOf(DataIntegrityViolationException::class.java)
	}

	@Test
	fun `deletes only the given user's favorite`() {
		repository.save(FavoriteMovie("user-1", 1, "Movie", null))
		repository.save(FavoriteMovie("user-2", 1, "Movie", null))

		assertThat(repository.deleteByUserIdAndMovieId("user-1", 1)).isEqualTo(1)
		assertThat(repository.deleteByUserIdAndMovieId("user-1", 1)).isEqualTo(0)
		assertThat(repository.findByUserIdAndMovieId("user-2", 1)).isNotNull()
	}
}
