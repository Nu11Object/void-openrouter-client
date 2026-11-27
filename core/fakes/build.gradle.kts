plugins {
    alias(libs.plugins.voidappKmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.testing)
            api(projects.core.model)
            api(projects.core.auth)
            api(projects.core.data.catalog)
            api(projects.core.data.session)
            api(projects.core.data.settings)
            api(projects.core.data.chat)
            api(libs.kotlinx.coroutines.core)
            api(libs.file.kit.core)
        }
    }
}
