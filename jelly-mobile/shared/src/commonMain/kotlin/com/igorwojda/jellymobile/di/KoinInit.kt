package com.igorwojda.jellymobile.di

import com.igorwojda.jellymobile.feature.base.di.baseModule
import com.igorwojda.jellymobile.feature.favorites.di.featureFavoritesModule
import com.igorwojda.jellymobile.feature.movie.di.featureMovieModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

/** Starts the single Koin container with the modules of every feature the app ships. Platforms add their [config]. */
fun initializeKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(baseModule, featureMovieModule, featureFavoritesModule)
    }
}
