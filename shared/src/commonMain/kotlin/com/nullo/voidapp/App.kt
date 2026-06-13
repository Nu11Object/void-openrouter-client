package com.nullo.voidapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.nullo.voidapp.component.RootComponent
import com.nullo.voidapp.feature.auth.presentation.ui.AuthScreen

@Composable
fun App(
    platform: Platform,
    rootComponent: RootComponent
) {
    CompositionLocalProvider(LocalPlatform provides platform) {
        Children(
            stack = rootComponent.stack,
            animation = stackAnimation(fade())
        ) {
            when (val instance = it.instance) {
                is RootComponent.Child.Auth -> AuthScreen(
                    authComponent = instance.component
                )
            }
        }
    }
}
