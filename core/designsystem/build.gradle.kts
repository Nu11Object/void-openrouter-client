plugins {
    alias(libs.plugins.voidappKmpLibrary)
    alias(libs.plugins.voidappKmpCompose)
    alias(libs.plugins.valkyrie)
}

valkyrie {
    packageName = "com.nullo.voidapp.core.designsystem.icon"
    generateAtSync = true

    iconPack {
        name = "Icons"
        targetSourceSet = "commonMain"
        autoMirror = false

        nested {
            name = "Default"
            sourceFolder = "default"
        }

        nested {
            name = "AutoMirrored"
            sourceFolder = "automirrored"
            autoMirror = true
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.utils)

            implementation(libs.markdown.m3)
        }
    }
}
