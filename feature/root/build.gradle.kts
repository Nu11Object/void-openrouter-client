plugins {
    alias(libs.plugins.voidappFeature)
    alias(libs.plugins.voidappKmpSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.auth)
            api(projects.feature.settings)
            api(projects.feature.catalog)
            api(projects.feature.chat)

            api(projects.core.designsystem)
            api(libs.kotlinx.coroutines.core)

            implementation(projects.core.data.settings)
            implementation(projects.core.data.chat)
            implementation(projects.core.security)

            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.mvikotlin.main)
        }
    }
}
