import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * iOS composition root: builds the single static `Shared` framework the iOS app links
 * (via `embedAndSignAppleFrameworkForXcode`). Apply next to [FeatureConventionPlugin].
 */
class IosFrameworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType(KotlinNativeTarget::class.java).configureEach {
                    binaries.framework {
                        baseName = "Shared"
                        isStatic = true
                        // Kotlin/Native cannot infer a bundle ID for a static framework; set it explicitly to silence
                        // the warning.
                        binaryOption("bundleId", "com.igorwojda.jellymobile.shared")
                    }
                }
            }
        }
}
