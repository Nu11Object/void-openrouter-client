package com.nullo.voidapp.feature.root.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.feature.auth.presentation.ui.AuthScreen
import com.nullo.voidapp.feature.settings.presentation.ui.SettingsScreen

@Composable
fun RootContent(
    rootComponent: RootComponent,
    modifier: Modifier = Modifier,
    windowTitleBar: (@Composable () -> Unit)? = null
) {
    val appTheme by rootComponent.appTheme.collectAsStateWithLifecycle()
    val theme = appTheme ?: return

    VoidTheme(appTheme = theme) {
        Column(modifier = modifier.fillMaxSize()) {
            windowTitleBar?.invoke()

            Children(
                stack = rootComponent.stack,
                animation = stackAnimation(fade() + scale()),
                modifier = Modifier.weight(1f)
            ) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Initial -> {}
                    is RootComponent.Child.Auth -> AuthScreen(instance.component)
                    is RootComponent.Child.Settings -> SettingsScreen(instance.component)
                }
            }
        }
    }
}
