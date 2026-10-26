plugins {
    alias(libs.plugins.voidappKmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.designsystem)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(projects.core.datastore)
        }
    }
}
