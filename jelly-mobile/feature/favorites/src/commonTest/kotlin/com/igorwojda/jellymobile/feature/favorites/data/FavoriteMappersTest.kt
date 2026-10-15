package com.igorwojda.jellymobile.feature.favorites.data

import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteMovieEntity
import com.igorwojda.jellymobile.feature.favorites.data.database.SyncState
import com.igorwojda.jellymobile.feature.favorites.data.database.toDomain
import com.igorwojda.jellymobile.feature.favorites.data.database.toEntity
import com.igorwojda.jellymobile.feature.favorites.data.model.FavoriteMovieResponseModel
import com.igorwojda.jellymobile.feature.favorites.data.model.toDomain
import com.igorwojda.jellymobile.feature.favorites.domain.model.FavoriteMovie
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteMappersTest {
    @Test
    fun `maps a backend favorite`() {
        assertEquals(dune, FavoriteMovieResponseModel(id = 1, title = "Dune", posterUrl = "https://example.com/dune.jpg").toDomain())
    }

    @Test
    fun `maps a backend favorite without a poster`() {
        assertEquals(FavoriteMovie(id = 2, title = "Alien", posterUrl = null), FavoriteMovieResponseModel(id = 2, title = "Alien").toDomain())
    }

    @Test
    fun `maps a favorite to an entity and back`() {
        val entity = dune.toEntity(addedAtMillis = 42, syncState = SyncState.PENDING_ADD)

        assertEquals(
            FavoriteMovieEntity(
                id = 1,
                title = "Dune",
                posterUrl = "https://example.com/dune.jpg",
                addedAtMillis = 42,
                syncState = SyncState.PENDING_ADD,
            ),
            entity,
        )
        assertEquals(dune, entity.toDomain())
    }

    private companion object {
        val dune = FavoriteMovie(id = 1, title = "Dune", posterUrl = "https://example.com/dune.jpg")
    }
}
