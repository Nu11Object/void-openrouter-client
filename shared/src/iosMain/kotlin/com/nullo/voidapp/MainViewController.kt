package com.nullo.voidapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.nullo.voidapp.component.RootComponent
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

fun MainViewController() = ComposeUIViewController {
    val rootComponent = remember {
        getKoin().get<RootComponent> {
            parametersOf(DefaultComponentContext(ApplicationLifecycle()))
        }
    }
    val appTheme by rootComponent.appTheme.collectAsStateWithLifecycle()

    VoidTheme(appTheme = appTheme) {
        App(
            platform = Platform.IOS,
            rootComponent = rootComponent
        )
    }
}
