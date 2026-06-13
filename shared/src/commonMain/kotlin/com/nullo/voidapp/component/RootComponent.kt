package com.nullo.voidapp.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        data class Auth(val component: AuthComponent) : Child
    }
}
