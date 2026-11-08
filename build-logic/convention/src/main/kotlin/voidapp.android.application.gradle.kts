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

val signingEnv = listOf(
    "ANDROID_KEYSTORE_PATH",
    "ANDROID_KEYSTORE_PASSWORD",
    "ANDROID_KEY_ALIAS",
    "ANDROID_KEY_PASSWORD",
).associateWith { providers.environmentVariable(it) }

extensions.configure<ApplicationExtension> {
    namespace = "com.nullo.voidapp"
    compileSdk = requiredVersion("android-compileSdk").toInt()
    compileSdkMinor = requiredVersion("android-compileSdkMinor").toInt()

    defaultConfig {
        minSdk = requiredVersion("android-minSdk").toInt()
        targetSdk = requiredVersion("android-targetSdk").toInt()
    }

    signingConfigs {
        if (signingEnv.values.all { it.isPresent }) {
            create("release") {
                storeFile = file(signingEnv.getValue("ANDROID_KEYSTORE_PATH").get())
                storePassword = signingEnv.getValue("ANDROID_KEYSTORE_PASSWORD").get()
                keyAlias = signingEnv.getValue("ANDROID_KEY_ALIAS").get()
                keyPassword = signingEnv.getValue("ANDROID_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = true
            }
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
