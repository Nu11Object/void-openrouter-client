import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.valkyrie)
}

valkyrie {
    packageName = "com.nullo.voidapp.core.designsystem.icon"
    generateAtSync = true

    iconPack {
        name = "Icons"
        targetSourceSet = "commonMain"
        autoMirror = false

        nested {
            name = "Default"
            sourceFolder = "default"
        }

        nested {
            name = "AutoMirrored"
            sourceFolder = "automirrored"
            autoMirror = true
        }
    }
}

kotlin {

    iosArm64()
    iosSimulatorArm64()

    jvm()

    android {
        namespace = "com.nullo.voidapp.core.designsystem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
        }
    }
}
