plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
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
    }
}
