plugins {
    alias(libs.plugins.jelly.feature)
    // Movie cache
    alias(libs.plugins.jelly.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Favorite toggle on the movie list and details screens
            implementation(projects.feature.favorites)
        }
    }
}
