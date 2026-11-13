plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpSerialization)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ionspin.bignum)
            api(libs.kotlinx.serialization.core)
            implementation(libs.compose.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
