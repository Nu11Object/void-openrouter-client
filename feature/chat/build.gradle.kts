plugins {
    alias(libs.plugins.voidappFeature)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)

            implementation(projects.core.data.catalog)
            implementation(projects.core.data.chat)
            implementation(projects.core.data.session)

            implementation(libs.file.kit.core)
            implementation(libs.file.kit.dialogs.compose)
            implementation(libs.file.kit.coil)
            implementation(libs.markdown.core)
            implementation(libs.markdown.m3)
            implementation(libs.markdown.code)
            implementation(libs.markdown.coil)
            implementation(libs.coil.ktor3)
            implementation(libs.kotlinx.datetime)
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
