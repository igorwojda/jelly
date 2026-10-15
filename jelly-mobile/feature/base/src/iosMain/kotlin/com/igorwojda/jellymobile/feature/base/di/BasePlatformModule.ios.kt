package com.igorwojda.jellymobile.feature.base.di

import com.igorwojda.jellymobile.feature.base.data.database.DatabaseDirectory
import org.koin.dsl.module

internal actual val basePlatformModule =
    module {
        single { DatabaseDirectory() }
    }
