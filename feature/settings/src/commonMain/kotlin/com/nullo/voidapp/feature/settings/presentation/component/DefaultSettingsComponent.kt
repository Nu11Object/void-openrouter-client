package com.nullo.voidapp.feature.settings.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.utils.decompose.componentScope
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.Intent
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStoreFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DefaultSettingsComponent(
    private val onNavigateBack: () -> Unit,
    private val onSignedOut: () -> Unit,
    private val settingsStoreFactory: SettingsStoreFactory,
    private val componentContext: ComponentContext,
) : SettingsComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val store = instanceKeeper.getStore { settingsStoreFactory.create() }

    init {
        observeLabels()
    }

    override val model: Value<SettingsStore.State> = store.asValue()

    override fun onBackClicked() {
        store.accept(Intent.NavigateBack)
    }

    override fun onApiKeyChanged(input: String) {
        store.accept(Intent.ChangeApiKey(input))
    }

    override fun onSubmitApiKeyClicked() {
        store.accept(Intent.SubmitApiKey)
    }

    override fun onThemeClicked(theme: AppTheme) {
        store.accept(Intent.ChangeAppTheme(theme))
    }

    override fun onRequestSignOutClicked() {
        store.accept(Intent.RequestSignOut)
    }

    override fun onConfirmSignOutClicked() {
        store.accept(Intent.ConfirmSignOut)
    }

    override fun onDialogDismissed() {
        store.accept(Intent.DismissDialog)
    }

    private fun observeLabels() {
        store.labels
            .onEach { label ->
                when (label) {
                    SettingsStore.Label.NavigatedBack -> onNavigateBack()
                    SettingsStore.Label.SignedOut -> onSignedOut()
                }
            }
            .launchIn(scope)
    }
}
