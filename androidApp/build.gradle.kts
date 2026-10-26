import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.voidappAndroidApplication)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(projects.shared)

    implementation(projects.feature.auth)
    implementation(projects.feature.root)

    implementation(libs.koin.android)
    implementation(libs.decompose.core)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashScreen)
}

android {
    defaultConfig {
        applicationId = "com.nullo.voidapp"
        versionCode = 1
        versionName = "1.0"
    }
}
