plugins {
    alias(libs.plugins.voidappFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.auth)
            implementation(projects.core.security)

            implementation(libs.ktor.http)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity)
            implementation(libs.androidx.browser)
            implementation(libs.koin.android)
        }
    }
}
