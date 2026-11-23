plugins {
    alias(libs.plugins.voidappFeature)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.auth)
            implementation(projects.core.security)

            implementation(libs.ktor.http)
        }
        commonTest.dependencies {
            implementation(projects.core.fakes)
            implementation(projects.core.testing)

            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity)
            implementation(libs.androidx.browser)
            implementation(libs.koin.android)
        }
    }
}
