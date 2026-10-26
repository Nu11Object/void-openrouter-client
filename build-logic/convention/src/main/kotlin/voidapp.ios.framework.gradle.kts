import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType(KotlinNativeTarget::class.java).configureEach {
            binaries.framework {
                baseName = project.name.replaceFirstChar { it.uppercase() }
                isStatic = true
            }
        }
    }
}
