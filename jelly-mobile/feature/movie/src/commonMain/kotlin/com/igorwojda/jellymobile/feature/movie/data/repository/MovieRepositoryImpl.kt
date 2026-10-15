package com.igorwojda.jellymobile.feature.movie.data.repository

import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.data.remote.fetchWithCacheFallback
import com.igorwojda.jellymobile.feature.base.data.remote.mapDataErrors
import com.igorwojda.jellymobile.feature.movie.data.datasource.local.MovieLocalDataSource
import com.igorwojda.jellymobile.feature.movie.data.datasource.remote.MovieRemoteDataSource
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieCategory
import com.igorwojda.jellymobile.feature.movie.domain.model.MovieDetails
import com.igorwojda.jellymobile.feature.movie.domain.model.MoviePage
import com.igorwojda.jellymobile.feature.movie.domain.repository.MovieRepository

/**
 * Movies from the first available remote source (Jelly backend, then TMDB directly), cached on the device. When no
 * remote source is reachable, the cached copy is returned instead. Failures are thrown as `DataException`.
 */
internal class MovieRepositoryImpl(
    private val remoteDataSources: RemoteDataSourceChain<MovieRemoteDataSource>,
    private val localDataSource: MovieLocalDataSource,
) : MovieRepository {
    override suspend fun getMovies(
        category: MovieCategory,
        page: Int,
    ): MoviePage =
        mapDataErrors {
            fetchWithCacheFallback(
                fetch = { remoteDataSources.request { it.getMovies(category, page) } },
                saveToCache = { localDataSource.saveMovies(category, it) },
                readFromCache = { localDataSource.getMovies(category, page) },
            )
        }

    override suspend fun getMovieDetails(movieId: Long): MovieDetails =
        mapDataErrors {
            fetchWithCacheFallback(
                fetch = { remoteDataSources.request { it.getMovieDetails(movieId) } },
                saveToCache = { localDataSource.saveMovieDetails(it) },
                readFromCache = { localDataSource.getMovieDetails(movieId) },
            )
        }
}
