plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.data.session)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.utils)

            implementation(libs.ionspin.bignum)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
