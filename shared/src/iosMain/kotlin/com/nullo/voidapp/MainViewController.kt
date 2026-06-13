package com.nullo.voidapp

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.nullo.voidapp.component.RootComponent
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

object RootComponentHolder : KoinComponent {
    fun create(): RootComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle)
        return get<RootComponent> { parametersOf(context) }
    }
}

fun MainViewController() = ComposeUIViewController {
    VoidTheme {
        App(
            platform = Platform.IOS,
            rootComponent = remember { RootComponentHolder.create() }
        )
    }
}
