plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(projects.core.database)
            implementation(projects.core.data.session)
            implementation(projects.core.utils)

            implementation(libs.ktor.client.core)
            implementation(libs.file.kit.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
