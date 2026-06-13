package com.nullo.voidapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.nullo.voidapp.component.RootComponent
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Close
import com.nullo.voidapp.core.designsystem.icon.default.Fullscreen
import com.nullo.voidapp.core.designsystem.icon.default.Minimize
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.di.initKoin
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin
import java.awt.Dimension

private const val WINDOW_TITLE = "Void"

fun main() {
    initKoin()

    application {
        val windowState = rememberWindowState()
        val lifecycle = remember { LifecycleRegistry() }

        val rootComponent = remember {
            getKoin().get<RootComponent> {
                parametersOf(DefaultComponentContext(lifecycle))
            }
        }

        LifecycleController(
            lifecycleRegistry = lifecycle,
            windowState = windowState
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = WINDOW_TITLE,
            state = windowState,
            undecorated = true,
            transparent = true
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(
                    WIDTH_DP_EXPANDED_LOWER_BOUND,
                    HEIGHT_DP_MEDIUM_LOWER_BOUND
                )
            }

            VoidTheme {
                Column(
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    WindowDraggableArea {
                        CustomTitleBar(
                            windowState = windowState,
                            onClose = ::exitApplication
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    App(
                        platform = Platform.DESKTOP,
                        rootComponent = rootComponent
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomTitleBar(
    windowState: WindowState,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.surfaceDim),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))
        Text(
            text = WINDOW_TITLE,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        WindowControlButton(
            icon = Icons.Default.Minimize,
            contentDescription = "Minimize window"
        ) {
            windowState.isMinimized = true
        }

        val isMaximized = remember(windowState.placement) {
            windowState.placement == WindowPlacement.Maximized
        }
        WindowControlButton(
            icon = Icons.Default.Fullscreen,
            contentDescription = if (isMaximized) {
                "Restore window"
            } else {
                "Maximize window"
            }
        ) {
            windowState.placement = if (isMaximized) {
                WindowPlacement.Floating
            } else {
                WindowPlacement.Maximized
            }
        }

        WindowControlButton(
            icon = Icons.Default.Close,
            contentDescription = "Close window"
        ) {
            onClose()
        }
    }
}

@Composable
private fun WindowControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = contentDescription
        )
    }
}
