plugins {
    alias(libs.plugins.voidappFeature)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.designsystem)

            implementation(projects.core.security)
            implementation(projects.core.auth)
            implementation(projects.core.data.settings)
            implementation(projects.core.data.catalog)
            implementation(projects.core.data.session)
            implementation(projects.core.data.chat)
        }
        commonTest.dependencies {
            implementation(projects.core.fakes)
            implementation(projects.core.testing)

            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.coroutines)
        }
    }
}
