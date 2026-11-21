plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.security)
            implementation(projects.core.utils)

            api(libs.koin.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
