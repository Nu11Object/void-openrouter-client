import com.android.build.api.dsl.ApplicationExtension
import com.nullo.voidapp.buildlogic.library
import com.nullo.voidapp.buildlogic.requiredVersion
import org.gradle.kotlin.dsl.configure

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("voidapp.quality")
}

extensions.configure<ApplicationExtension> {
    namespace = "com.nullo.voidapp"
    compileSdk = requiredVersion("android-compileSdk").toInt()
    compileSdkMinor = requiredVersion("android-compileSdkMinor").toInt()

    defaultConfig {
        minSdk = requiredVersion("android-minSdk").toInt()
        targetSdk = requiredVersion("android-targetSdk").toInt()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("implementation", library("compose-uiToolingPreview"))
    add("debugImplementation", library("compose-uiTooling"))
}
