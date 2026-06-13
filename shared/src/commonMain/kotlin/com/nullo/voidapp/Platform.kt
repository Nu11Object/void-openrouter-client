package com.nullo.voidapp

import androidx.compose.runtime.staticCompositionLocalOf

enum class Platform {
    ANDROID, IOS, DESKTOP
}

val LocalPlatform = staticCompositionLocalOf<Platform> {
    error("LocalPlatform not provided. Wrap your root composable with: " +
            "CompositionLocalProvider(LocalPlatform provides platform) { /* content */ }"
    )
}