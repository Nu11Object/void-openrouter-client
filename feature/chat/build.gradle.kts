plugins {
    alias(libs.plugins.voidappFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)

            implementation(projects.core.data.catalog)
            implementation(projects.core.data.chat)
            implementation(projects.core.data.session)

            implementation(libs.markdown.core)
            implementation(libs.markdown.m3)
            implementation(libs.markdown.code)
            implementation(libs.markdown.coil)
            implementation(libs.coil.ktor3)
        }
    }
}
