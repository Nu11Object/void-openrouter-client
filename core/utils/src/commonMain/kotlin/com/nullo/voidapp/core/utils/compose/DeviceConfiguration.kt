package com.nullo.voidapp.core.utils.compose

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/**
 * Represents the layout category of the current device.
 * Used to determine screen size and orientation to build adaptive Composable UIs.
 */
enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    companion object {

        /**
         * Maps the provided [WindowSizeClass] into a unified [DeviceConfiguration].
         *
         * Exposed as `internal`; use [rememberDeviceConfiguration] from Composable code instead.
         *
         * @param windowSizeClass The window sizing metrics provided by the platform.
         * @return The matching [DeviceConfiguration] for UI adaptation.
         */
        internal fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val isWidthMedium =
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
            val isWidthExpanded =
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
            val isHeightMedium =
                windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)

            return when {
                isWidthExpanded && !isHeightMedium -> MOBILE_LANDSCAPE
                isWidthExpanded -> DESKTOP
                isWidthMedium && isHeightMedium -> TABLET_PORTRAIT
                isWidthMedium -> TABLET_LANDSCAPE
                else -> MOBILE_PORTRAIT
            }
        }
    }
}
