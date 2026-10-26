package com.nullo.voidapp.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.library(alias: String): Provider<MinimalExternalModuleDependency> =
    libs.findLibrary(alias).orElseThrow {
        IllegalArgumentException("Library '$alias' is not declared in libs.versions.toml")
    }

internal fun Project.bundle(alias: String): Provider<ExternalModuleDependencyBundle> =
    libs.findBundle(alias).orElseThrow {
        IllegalArgumentException("Bundle '$alias' is not declared in libs.versions.toml")
    }

internal fun Project.requiredVersion(alias: String): String =
    libs.findVersion(alias).orElseThrow {
        IllegalArgumentException("Version '$alias' is not declared in libs.versions.toml")
    }.requiredVersion

/**
 * Namespaces follow the Gradle project path, e.g. `:core:data:chat` becomes
 * `com.nullo.voidapp.core.data.chat`.
 */
internal fun Project.defaultNamespace(): String =
    "com.nullo.voidapp." + path
        .trim(':')
        .split(':')
        .joinToString(".") { it.lowercase().replace('-', '_') }

internal fun KotlinMultiplatformExtension.androidKmpTarget(): KotlinMultiplatformAndroidLibraryTarget =
    (this as ExtensionAware).extensions.getByType()

internal fun KotlinMultiplatformExtension.configureAndroidKmpTarget(
    namespace: String,
    compileSdk: Int,
    compileSdkMinor: Int,
    minSdk: Int,
    enableAndroidResources: Boolean = false,
) {
    androidKmpTarget().apply {
        this.namespace = namespace
        compileSdk {
            version = release(compileSdk) {
                minorApiLevel = compileSdkMinor
            }
        }
        this.minSdk = minSdk
        androidResources.enable = enableAndroidResources
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}
