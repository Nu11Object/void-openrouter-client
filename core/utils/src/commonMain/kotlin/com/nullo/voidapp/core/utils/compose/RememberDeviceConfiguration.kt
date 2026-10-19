package com.nullo.voidapp.core.utils.compose

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Returns the current [DeviceConfiguration], automatically updating whenever the window's
 * size class changes (e.g. on rotation, resizing, or fold/unfold).
 *
 * @return The [DeviceConfiguration] matching the current window size.
 */
@Composable
fun rememberDeviceConfiguration(): DeviceConfiguration {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return remember(windowSizeClass) {
        DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    }
}
