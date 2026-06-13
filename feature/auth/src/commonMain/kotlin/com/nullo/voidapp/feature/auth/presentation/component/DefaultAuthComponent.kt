package com.nullo.voidapp.feature.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore
import com.nullo.voidapp.feature.auth.presentation.store.AuthStoreFactory

internal class DefaultAuthComponent(
    private val authStoreFactory: AuthStoreFactory,
    private val onAuthFinish: () -> Unit,
    private val componentContext: ComponentContext,
) : AuthComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { authStoreFactory.create() }

    override val model: Value<AuthStore.State> = store.asValue()

    override fun onOAuthClicked() {
        store.accept(AuthStore.Intent.SignInViaOpenRouter)
    }

    override fun onCancelOAuthClicked() {
        store.accept(AuthStore.Intent.CancelOAuth)
    }

    override fun onApiKeyInputChanged(apiKey: String) {
        store.accept(AuthStore.Intent.ChangeApiKey(apiKey))
    }

    override fun onSubmitApiKeyClicked() {
        store.accept(AuthStore.Intent.SubmitApiKey)
    }

    override fun onDismissErrorClicked() {
        store.accept(AuthStore.Intent.DismissError)
    }

    override fun onAuthFinished() {
        onAuthFinish()
    }
}
