package com.nullo.voidapp.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A CompositionLocal that indicates whether the dark theme is currently active.
 *
 * This value is managed by [VoidTheme] and should not be overridden manually.
 * Defaults to `true` if accessed outside of [VoidTheme].
 */
val LocalDarkTheme = staticCompositionLocalOf { true }
