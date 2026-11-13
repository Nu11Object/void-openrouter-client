import com.nullo.voidapp.buildlogic.library
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("de.infix.testBalloon")
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.commonTest.dependencies {
        implementation(library("testBalloon-core"))
        implementation(library("testBalloon-kotest"))
    }
}
