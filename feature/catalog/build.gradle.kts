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
            implementation(projects.core.testing)
        }
    }
}
