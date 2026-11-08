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

val appVersion = providers.gradleProperty("app.version").get()
val (major, minor, patch) = appVersion.split('.')

android {
    defaultConfig {
        applicationId = "com.nullo.voidapp"
        versionName = appVersion
        versionCode = major.toInt() * 10_000 + minor.toInt() * 100 + patch.toInt()
    }
}
