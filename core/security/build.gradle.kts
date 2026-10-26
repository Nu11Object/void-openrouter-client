plugins {
    alias(libs.plugins.voidappKmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.datastore)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.android.tink)
        }
        jvmMain.dependencies {
            implementation(libs.java.keyring)
        }
    }
}
