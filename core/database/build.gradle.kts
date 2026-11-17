plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappRoom)
    alias(libs.plugins.voidappTestBalloon)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)

            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(projects.core.desktop)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
        jvmTest.dependencies {
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
