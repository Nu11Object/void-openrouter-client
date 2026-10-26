plugins {
    alias(libs.plugins.voidappFeature)
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
    }
}
