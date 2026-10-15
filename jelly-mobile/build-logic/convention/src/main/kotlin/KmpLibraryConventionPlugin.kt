import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Kotlin Multiplatform library targeting Android and iOS, with Compose Multiplatform UI and kotlinx.serialization.
 *
 * The Android namespace is derived from the module path, e.g. `:feature:movie` → `com.igorwojda.jellymobile.feature.movie`.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("kotlinMultiplatform"))
            pluginManager.apply(libs.pluginId("androidMultiplatformLibrary"))
            pluginManager.apply(libs.pluginId("composeMultiplatform"))
            pluginManager.apply(libs.pluginId("composeCompiler"))
            pluginManager.apply(libs.pluginId("kotlinSerialization"))

            extensions.configure<KotlinMultiplatformExtension> {
                iosArm64()
                iosSimulatorArm64()

                compilerOptions {
                    // expect/actual classes (platform-specific types, e.g. Room database constructors) are still Beta.
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    namespace = "com.igorwojda.jellymobile" + path.replace(':', '.')
                    compileSdk = libs.version("android-compileSdk").toInt()
                    minSdk = libs.version("android-minSdk").toInt()

                    compilerOptions {
                        jvmTarget.set(libs.jvmTarget)
                    }

                    // Android host (JVM) unit tests
                    withHostTest {
                        // android.* stubs (e.g. the store logger's android.util.Log) return defaults instead of throwing
                        isReturnDefaultValues = true
                    }
                }

                sourceSets.commonMain.dependencies {
                    implementation(libs.lib("compose-runtime"))
                    implementation(libs.lib("compose-foundation"))
                    implementation(libs.lib("compose-material3"))
                    implementation(libs.lib("compose-ui"))
                    implementation(libs.lib("compose-components-resources"))
                    implementation(libs.lib("compose-uiToolingPreview"))
                    implementation(libs.lib("kotlinx-serialization-json"))
                }
                // Shared by all test source sets (androidHostTest, iosTest)
                sourceSets.commonTest.dependencies {
                    implementation(libs.lib("kotlin-test"))
                    implementation(libs.lib("kotlinx-coroutines-test"))
                    // Fake HTTP responses (e.g. real Ktor exceptions for 4xx / 5xx)
                    implementation(libs.lib("ktor-client-mock"))
                }
            }

            dependencies {
                // Renders @Preview in the IDE; the KMP android library plugin has no debug-only configuration.
                "androidRuntimeClasspath"(libs.lib("compose-uiTooling"))
            }
        }
}
