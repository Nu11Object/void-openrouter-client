import com.nullo.voidapp.buildlogic.androidKmpTarget
import com.nullo.voidapp.buildlogic.bundle
import com.nullo.voidapp.buildlogic.library
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.commonMain.dependencies {
            implementation(bundle("compose-multiplatform"))
        }
    }
}

pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
    extensions.configure<KotlinMultiplatformExtension> {
        androidKmpTarget().androidResources.enable = true
    }

    dependencies {
        add("androidRuntimeClasspath", library("compose-uiTooling"))
    }
}
