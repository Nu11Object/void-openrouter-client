plugins {
    alias(libs.plugins.voidappFeature)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)

            implementation(projects.core.data.catalog)
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
