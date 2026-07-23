package com.nullo.voidapp.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    val appTheme: StateFlow<AppTheme>

    sealed interface Child {

        data class Auth(val component: AuthComponent) : Child
    }
}
