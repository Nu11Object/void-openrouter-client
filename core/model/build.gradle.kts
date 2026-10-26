plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ionspin.bignum)
            api(libs.kotlinx.serialization.core)
            implementation(libs.compose.runtime)
        }
    }
}
