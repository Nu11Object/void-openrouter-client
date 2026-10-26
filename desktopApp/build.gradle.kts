import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun

plugins {
    alias(libs.plugins.voidappDesktopApplication)
}

dependencies {
    implementation(projects.shared)

    implementation(projects.feature.root)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.window.core)
}

compose.desktop {
    application {
        mainClass = "com.nullo.voidapp.MainKt"
        jvmArgs += "--enable-native-access=ALL-UNNAMED"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )

            packageName = "Void"
            packageVersion = "1.0.0"
            description = "OpenRouter client for Android, iOS and Desktop (Kotlin Multiplatform)"
            vendor = "nullo"
            copyright = "© 2026 nullo"
            licenseFile.set(project.file("../LICENSE"))

            modules("java.instrument", "java.management", "jdk.security.auth", "jdk.unsupported")

            macOS {
                bundleID = "com.nullo.voidapp.desktop"
                dockName = "Void"
                appCategory = "public.app-category.productivity"
                iconFile.set(project.file("icons/MacOS.icns"))
            }

            windows {
                upgradeUuid = "FDC0FAD2-890B-4967-BEE8-C7B85FBACA35"
                menuGroup = "Void"
                shortcut = true
                perUserInstall = true
                iconFile.set(project.file("icons/Windows.ico"))
            }

            linux {
                packageName = "voidapp"
                appCategory = "Utility"
                rpmLicenseType = "MIT"
                shortcut = true
                iconFile.set(project.file("icons/Linux.png"))
            }
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
