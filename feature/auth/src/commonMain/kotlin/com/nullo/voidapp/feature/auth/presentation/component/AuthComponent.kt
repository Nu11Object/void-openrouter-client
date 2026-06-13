package com.nullo.voidapp.feature.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore

interface AuthComponent {

    val model: Value<AuthStore.State>

    fun onOAuthClicked()

    fun onCancelOAuthClicked()

    fun onApiKeyInputChanged(apiKey: String)

    fun onSubmitApiKeyClicked()

    fun onDismissErrorClicked()

    fun onAuthFinished()

    fun interface Factory {

        fun create(
            onAuthFinished: () -> Unit,
            componentContext: ComponentContext
        ): AuthComponent
    }
}
