package com.nullo.voidapp

import androidx.compose.runtime.staticCompositionLocalOf

enum class Platform {
    ANDROID, IOS, DESKTOP
}

expect fun getPlatform(): Platform

val LocalPlatform = staticCompositionLocalOf<Platform> {
    error("LocalPlatform not provided. Wrap your root composable with PlatformProvider.")
}