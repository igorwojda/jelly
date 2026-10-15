plugins {
    alias(libs.plugins.jelly.android.application)
}

android {
    namespace = "com.igorwojda.jellymobile"

    defaultConfig {
        applicationId = "com.igorwojda.jellymobile"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.shared)
    // androidContext(): Android Context for the platform dependencies (e.g. Room database location)
    implementation(libs.koin.android)
}
