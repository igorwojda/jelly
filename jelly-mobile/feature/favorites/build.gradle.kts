plugins {
    alias(libs.plugins.jelly.feature)
    // Favorites saved on the device
    alias(libs.plugins.jelly.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Persists the client-generated user id the backend keys favorites by
            implementation(libs.multiplatformSettings.noArg)
        }
    }
}
