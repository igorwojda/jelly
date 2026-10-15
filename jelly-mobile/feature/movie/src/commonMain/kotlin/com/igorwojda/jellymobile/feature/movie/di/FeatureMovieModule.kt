package com.igorwojda.jellymobile.feature.movie.di

import com.igorwojda.jellymobile.feature.base.data.database.DatabaseDirectory
import com.igorwojda.jellymobile.feature.base.data.database.buildWithBundledDriver
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.di.BaseQualifier
import com.igorwojda.jellymobile.feature.movie.data.database.MovieDatabase
import com.igorwojda.jellymobile.feature.movie.data.database.movieDatabaseBuilder
import com.igorwojda.jellymobile.feature.movie.data.datasource.local.MovieLocalDataSource
import com.igorwojda.jellymobile.feature.movie.data.datasource.remote.BackendMovieRemoteDataSource
import com.igorwojda.jellymobile.feature.movie.data.datasource.remote.MovieRemoteDataSource
import com.igorwojda.jellymobile.feature.movie.data.datasource.remote.TmdbMovieRemoteDataSource
import com.igorwojda.jellymobile.feature.movie.data.repository.MovieRepositoryImpl
import com.igorwojda.jellymobile.feature.movie.domain.repository.MovieRepository
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMovieDetailsUseCase
import com.igorwojda.jellymobile.feature.movie.domain.usecase.GetMoviesUseCase
import com.igorwojda.jellymobile.feature.movie.presentation.moviedetails.MovieDetailsViewModel
import com.igorwojda.jellymobile.feature.movie.presentation.movielist.MovieListViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Data, domain and presentation dependencies of the movie feature. */
val featureMovieModule =
    module {
        // Data layer
        single {
            movieDatabaseBuilder(get<DatabaseDirectory>().filePath(MovieDatabase.FILE_NAME))
                // Only a cache: on a schema change it's cheaper to download the movies again than to migrate them.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .buildWithBundledDriver()
        }
        single { get<MovieDatabase>().movieDao() }
        singleOf(::MovieLocalDataSource)
        single<MovieRepository> {
            // Remote sources in priority order; the TMDB fallback exists only when a TMDB token is configured.
            val remoteDataSources: List<MovieRemoteDataSource> =
                listOfNotNull(
                    BackendMovieRemoteDataSource(get(BaseQualifier.BackendHttpClient)),
                    getOrNull<HttpClient>(BaseQualifier.TmdbHttpClient)?.let(::TmdbMovieRemoteDataSource),
                )
            MovieRepositoryImpl(RemoteDataSourceChain(remoteDataSources, healthTracker = get()), localDataSource = get())
        }

        // Domain layer
        factoryOf(::GetMoviesUseCase)
        factoryOf(::GetMovieDetailsUseCase)

        // Presentation layer
        viewModelOf(::MovieListViewModel)
        viewModelOf(::MovieDetailsViewModel)
    }
