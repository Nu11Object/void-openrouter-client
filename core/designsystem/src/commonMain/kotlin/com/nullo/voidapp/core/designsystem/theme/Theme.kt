package com.nullo.voidapp.core.designsystem.theme

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    surfaceDim = DarkSurfaceDim,

    primary = DarkPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimary = DarkOnPrimary,

    secondary = DarkSecondary,
    secondaryContainer = DarkSecondaryContainer,
    tertiary = DarkTertiary,
    tertiaryContainer = DarkTertiaryContainer,

    error = DarkError,
    errorContainer = DarkErrorContainer,

    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceDim = LightSurfaceDim,

    primary = LightPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimary = LightOnPrimary,

    secondary = LightSecondary,
    secondaryContainer = LightSecondaryContainer,
    tertiary = LightTertiary,
    tertiaryContainer = LightTertiaryContainer,

    error = LightError,
    errorContainer = LightErrorContainer,

    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
)

private val LightExtendedColors = ExtendedColors(
    success = LightSuccess,
    successContainer = LightSuccessContainer,
    warning = LightWarning
)

private val DarkExtendedColors = ExtendedColors(
    success = DarkSuccess,
    successContainer = DarkSuccessContainer,
    warning = DarkWarning
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

@Composable
fun VoidTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    Crossfade(
        targetState = darkTheme,
        animationSpec = tween(durationMillis = 500),
        label = "ThemeCrossfade"
    ) { isDark ->
        val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
        val extendedColors = if (isDark) DarkExtendedColors else LightExtendedColors

        CompositionLocalProvider(
            LocalExtendedColors provides extendedColors
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = googleSansFlexTypography(),
                content = content
            )
        }
    }
}
