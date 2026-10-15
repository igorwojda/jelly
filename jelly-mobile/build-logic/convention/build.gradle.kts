plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: the root build applies these plugins (apply false), so they're already on the classpath.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.jelly.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.jelly.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("feature") {
            id = libs.plugins.jelly.feature.get().pluginId
            implementationClass = "FeatureConventionPlugin"
        }
        register("room") {
            id = libs.plugins.jelly.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("iosFramework") {
            id = libs.plugins.jelly.ios.framework.get().pluginId
            implementationClass = "IosFrameworkConventionPlugin"
        }
    }
}
