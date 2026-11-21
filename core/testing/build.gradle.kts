plugins {
    alias(libs.plugins.voidappKmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.security)
        }
    }
}
