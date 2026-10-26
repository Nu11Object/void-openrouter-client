import com.nullo.voidapp.buildlogic.bundle
import com.nullo.voidapp.buildlogic.library

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(bundle("compose-multiplatform"))

    implementation(library("koin-core"))
    implementation(library("decompose-core"))
    implementation(library("decompose-compose"))
    implementation(library("essenty-lifecycle"))
    implementation(library("mvikotlin-core"))

    implementation(library("kotlinx-coroutinesSwing"))
}
