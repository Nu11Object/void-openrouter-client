package com.nullo.voidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.arkivanov.decompose.defaultComponentContext
import com.nullo.voidapp.component.RootComponent
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.feature.auth.presentation.AuthTabResultRegistrar
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        get<AuthTabResultRegistrar>().register(this)

        val rootComponent = get<RootComponent> {
            parametersOf(defaultComponentContext())
        }

        setContent {
            VoidTheme {
                App(
                    platform = Platform.ANDROID,
                    rootComponent = remember { rootComponent }
                )
            }
        }
    }
}
