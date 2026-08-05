package com.nullo.voidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.defaultComponentContext
import com.nullo.voidapp.feature.auth.presentation.AuthTabResultRegistrar
import com.nullo.voidapp.feature.root.component.RootComponent
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        get<AuthTabResultRegistrar>().register(this)

        val rootComponent = get<RootComponent> {
            parametersOf(defaultComponentContext())
        }

        setContent {
            App(rootComponent = rootComponent)
        }
    }
}
