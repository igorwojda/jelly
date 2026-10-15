package com.igorwojda.jellymobile.feature.base.di

import org.koin.core.module.Module

/** Platform-specific dependencies of [baseModule], e.g. the database location. */
internal expect val basePlatformModule: Module
