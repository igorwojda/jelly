package com.igorwojda.jellymobile.feature.base.di

import com.igorwojda.jellymobile.feature.base.data.database.DatabaseDirectory
import org.koin.dsl.module

// Context comes from androidContext() in the Android app's Koin start-up.
internal actual val basePlatformModule =
    module {
        single { DatabaseDirectory(context = get()) }
    }
