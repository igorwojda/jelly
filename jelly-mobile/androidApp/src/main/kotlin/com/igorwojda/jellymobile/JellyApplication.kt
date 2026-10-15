package com.igorwojda.jellymobile

import android.app.Application
import com.igorwojda.jellymobile.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class JellyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@JellyApplication)
        }
    }
}
