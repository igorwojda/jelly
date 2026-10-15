package com.igorwojda.jellymobile.feature.favorites.di

import com.igorwojda.jellymobile.feature.base.data.database.DatabaseDirectory
import com.igorwojda.jellymobile.feature.base.data.database.buildWithBundledDriver
import com.igorwojda.jellymobile.feature.base.data.remote.RemoteDataSourceChain
import com.igorwojda.jellymobile.feature.base.di.BaseQualifier
import com.igorwojda.jellymobile.feature.favorites.data.database.FavoriteDatabase
import com.igorwojda.jellymobile.feature.favorites.data.database.favoriteDatabaseBuilder
import com.igorwojda.jellymobile.feature.favorites.data.datasource.local.UserIdLocalDataSource
import com.igorwojda.jellymobile.feature.favorites.data.datasource.remote.BackendFavoriteRemoteDataSource
import com.igorwojda.jellymobile.feature.favorites.data.datasource.remote.FavoriteRemoteDataSource
import com.igorwojda.jellymobile.feature.favorites.data.repository.FavoriteRepositoryImpl
import com.igorwojda.jellymobile.feature.favorites.domain.repository.FavoriteRepository
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ObserveFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.RefreshFavoritesUseCase
import com.igorwojda.jellymobile.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.igorwojda.jellymobile.feature.favorites.presentation.favoritelist.FavoriteListViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Data, domain and presentation dependencies of the favorites feature. */
val featureFavoritesModule =
    module {
        // Data layer
        single { Settings() }
        singleOf(::UserIdLocalDataSource)
        single { favoriteDatabaseBuilder(get<DatabaseDirectory>().filePath(FavoriteDatabase.FILE_NAME)).buildWithBundledDriver() }
        single { get<FavoriteDatabase>().favoriteMovieDao() }
        single<FavoriteRemoteDataSource> {
            BackendFavoriteRemoteDataSource(get(BaseQualifier.BackendHttpClient), userIdLocalDataSource = get())
        }
        // Single instance: one sync queue for every screen
        single<FavoriteRepository> {
            FavoriteRepositoryImpl(
                remoteDataSources = RemoteDataSourceChain(listOf(get<FavoriteRemoteDataSource>()), healthTracker = get()),
                favoriteMovieDao = get(),
                applicationScope = get(BaseQualifier.ApplicationScope),
            )
        }

        // Domain layer
        factoryOf(::ObserveFavoritesUseCase)
        factoryOf(::RefreshFavoritesUseCase)
        factoryOf(::ToggleFavoriteUseCase)

        // Presentation layer
        viewModelOf(::FavoriteListViewModel)
    }
