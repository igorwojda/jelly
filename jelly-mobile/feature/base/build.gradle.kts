import java.util.Properties

plugins {
    alias(libs.plugins.jelly.kmp.library)
    alias(libs.plugins.buildConfig)
}

// Per-machine settings (not in VCS). Read through a provider, so the configuration cache tracks the file.
val localProperties =
    providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .map { text -> Properties().apply { load(text.reader()) } }

/** [key] from local.properties, then -P / gradle.properties. */
fun jellyProperty(key: String): Provider<String> =
    localProperties
        // null (key missing) leaves the provider without a value, so the next source is used
        .map { it.getProperty(key) }
        .orElse(providers.gradleProperty(key))

// TMDB API Read Access Token for the direct TMDB fallback. Empty disables the fallback.
val tmdbApiToken =
    jellyProperty("jelly.tmdbApiToken")
        .orElse(providers.environmentVariable("TMDB_API_TOKEN"))
        .orElse("")

if (tmdbApiToken.get().isBlank()) {
    logger.warn(
        "w: jelly.tmdbApiToken is not set: the app has no TMDB fallback and needs the backend. " +
            "Set it in local.properties or the TMDB_API_TOKEN environment variable.",
    )
}

buildConfig {
    packageName("com.igorwojda.jellymobile.feature.base")
    // Default in gradle.properties (Android emulator), overridable in local.properties
    buildConfigField("BACKEND_BASE_URL", jellyProperty("jelly.backendBaseUrl"))
    buildConfigField("TMDB_API_TOKEN", tmdbApiToken)
}

kotlin {
    // Every feature module depends on this module, so shared libraries are exposed as api.
    sourceSets {
        commonMain.dependencies {
            // Presentation (MVI)
            api(libs.flowmvi.core)
            api(libs.flowmvi.compose)
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.kotlinx.coroutines.core)

            // DI
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.compose.viewmodel)

            // Network
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Images
            api(libs.coil.compose)
            api(libs.coil.network.ktor3)

            // Local database: each feature declares its own Room database (jelly.room plugin), built here per platform
            api(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
