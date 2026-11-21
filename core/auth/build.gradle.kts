plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.core.security)

            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
