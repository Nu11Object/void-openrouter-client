plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpCompose)
    alias(libs.plugins.voidappIosFramework)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.network)
            implementation(projects.core.auth)
            implementation(projects.core.data.settings)
            implementation(projects.core.data.catalog)
            implementation(projects.core.data.chat)
            implementation(projects.core.data.session)
            implementation(projects.core.security)
            implementation(projects.core.datastore)
            implementation(projects.core.database)

            api(projects.feature.root)
            implementation(projects.feature.auth)
            implementation(projects.feature.settings)
            implementation(projects.feature.catalog)
            implementation(projects.feature.chat)

            implementation(libs.koin.core)
            implementation(libs.decompose.core)
            implementation(libs.essenty.lifecycle)
        }
    }
}
