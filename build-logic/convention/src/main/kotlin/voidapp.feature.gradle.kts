import com.nullo.voidapp.buildlogic.library
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("voidapp.kmp.library")
    id("voidapp.kmp.compose")
    id("voidapp.mvi")
    id("voidapp.decompose")
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:designsystem"))
        api(project(":core:utils"))

        api(library("koin-core"))
        implementation(library("kotlinx-coroutines-core"))
    }
}
