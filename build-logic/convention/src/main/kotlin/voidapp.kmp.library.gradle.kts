import com.nullo.voidapp.buildlogic.configureAndroidKmpTarget
import com.nullo.voidapp.buildlogic.defaultNamespace
import com.nullo.voidapp.buildlogic.requiredVersion
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

extensions.configure<KotlinMultiplatformExtension> {
    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }

    configureAndroidKmpTarget(
        namespace = project.defaultNamespace(),
        compileSdk = project.requiredVersion("android-compileSdk").toInt(),
        compileSdkMinor = project.requiredVersion("android-compileSdkMinor").toInt(),
        minSdk = project.requiredVersion("android-minSdk").toInt(),
    )
}
