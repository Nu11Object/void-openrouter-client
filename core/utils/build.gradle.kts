plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpCompose)
}

compose.resources {
    publicResClass = true
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.mvikotlin.core)
            api(libs.decompose.core)
            api(libs.kotlinx.coroutines.core)

            implementation(libs.essenty.lifecycle)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.androidx.window.core)
        }
    }
}
