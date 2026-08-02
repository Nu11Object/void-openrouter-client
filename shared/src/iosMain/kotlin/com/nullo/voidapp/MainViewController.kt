package com.nullo.voidapp

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.nullo.voidapp.feature.root.component.RootComponent
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

fun MainViewController() = ComposeUIViewController {
    App(rootComponent = RootHolder.rootComponent)
}

private object RootHolder {
    val rootComponent: RootComponent by lazy {
        getKoin().get<RootComponent> {
            parametersOf(DefaultComponentContext(ApplicationLifecycle()))
        }
    }
}
