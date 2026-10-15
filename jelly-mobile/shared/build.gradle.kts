plugins {
    alias(libs.plugins.jelly.feature)
    alias(libs.plugins.jelly.ios.framework)
}

kotlin {
    // Composition root: app shell (theme, bottom navigation) wiring the feature screens together.
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.movie)
            implementation(projects.feature.favorites)

            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
        }
    }
}
