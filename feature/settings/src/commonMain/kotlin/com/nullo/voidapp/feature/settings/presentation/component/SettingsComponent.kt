package com.nullo.voidapp.feature.settings.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore

interface SettingsComponent {

    val model: Value<SettingsStore.State>

    fun onBackClicked()

    fun onApiKeyChanged(input: String)

    fun onSubmitApiKeyClicked()

    fun onThemeClicked(theme: AppTheme)

    fun onRequestSignOutClicked()

    fun onConfirmSignOutClicked()

    fun onDialogDismissed()

    fun interface Factory {

        fun create(
            onNavigateBack: () -> Unit,
            onSignedOut: () -> Unit,
            componentContext: ComponentContext
        ): SettingsComponent
    }
}
