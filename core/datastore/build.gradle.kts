plugins {
    alias(libs.plugins.voidappKmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            api(libs.androidx.datastore.preferences)
            implementation(libs.okio)
        }
        jvmMain.dependencies {
            implementation(projects.core.desktop)
        }
    }
}
