plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappRoom)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
