package com.nullo.voidapp.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nullo.voidapp.core.designsystem.icon.default.LogoDark
import com.nullo.voidapp.core.designsystem.icon.default.LogoLight
import com.nullo.voidapp.core.designsystem.theme.LocalDarkTheme

/**
 * Theme-aware logo icon: resolves to the light or dark variant
 * depending on the current system theme.
 */
val Icons.Logo: ImageVector
    @Composable
    get() = if (LocalDarkTheme.current) Icons.Default.LogoDark else Icons.Default.LogoLight
