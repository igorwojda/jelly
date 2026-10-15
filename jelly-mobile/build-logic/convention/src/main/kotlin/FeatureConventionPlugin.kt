import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature module: a [KmpLibraryConventionPlugin] library that depends on `:feature:base`, which provides
 * the MVI, DI, network, image loading and shared UI building blocks.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(KmpLibraryConventionPlugin::class.java)

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    api(project(":feature:base"))
                }
            }
        }
}
