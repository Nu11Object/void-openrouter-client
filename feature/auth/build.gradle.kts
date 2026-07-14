import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    iosArm64()
    iosSimulatorArm64()

    jvm()

    android {
        namespace = "com.nullo.voidapp.feature.auth"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {}
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.browser)

            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(projects.core.security)
            implementation(projects.core.utils)
            implementation(projects.core.designsystem)

            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)

            implementation(libs.mvikotlin.core)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.coroutines)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.koin.core)
            implementation(libs.koin.compose) // todo: remove soon

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            implementation(libs.mvikotlin.main)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}