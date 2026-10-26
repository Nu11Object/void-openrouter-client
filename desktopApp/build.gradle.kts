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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.nullo.voidapp"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
