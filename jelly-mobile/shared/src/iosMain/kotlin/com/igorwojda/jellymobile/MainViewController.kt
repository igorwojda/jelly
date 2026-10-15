package com.igorwojda.jellymobile

import androidx.compose.ui.window.ComposeUIViewController
import com.igorwojda.jellymobile.di.initializeKoin
import platform.UIKit.UIViewController

private val koinInitialization by lazy { initializeKoin() }

/** iOS entry point, called from SwiftUI (`ContentView.swift`). */
@Suppress("FunctionName", "ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    koinInitialization
    return ComposeUIViewController { App() }
}
