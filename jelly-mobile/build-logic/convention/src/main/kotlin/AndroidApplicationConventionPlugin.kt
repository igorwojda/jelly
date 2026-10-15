import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Android application hosting the shared Compose Multiplatform UI: SDK versions, JVM target and release build type.
 * The app module declares only its identity (namespace, application id, version) and the modules it ships.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("androidApplication"))
            pluginManager.apply(libs.pluginId("composeCompiler"))

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(libs.jvmTarget)
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.version("android-compileSdk").toInt()

                defaultConfig {
                    minSdk = libs.version("android-minSdk").toInt()
                    targetSdk = libs.version("android-targetSdk").toInt()
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = libs.javaVersion
                    targetCompatibility = libs.javaVersion
                }
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                "implementation"(libs.lib("androidx-activity-compose"))
            }
        }
}
