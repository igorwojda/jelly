import androidx.room3.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Room database in a [KmpLibraryConventionPlugin] module: Room runtime with the bundled SQLite driver, the Room
 * compiler (KSP) for every target, and exported schemas in `schemas/`.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("ksp"))
            pluginManager.apply(libs.pluginId("room"))

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.lib("room-runtime"))
                    implementation(libs.lib("sqlite-bundled"))
                }
            }

            dependencies {
                listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { configuration ->
                    configuration(libs.lib("room-compiler"))
                }
            }
        }
}
