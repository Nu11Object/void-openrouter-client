plugins {
    alias(libs.plugins.voidappFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)

            implementation(projects.core.data.catalog)
        }
    }
}
