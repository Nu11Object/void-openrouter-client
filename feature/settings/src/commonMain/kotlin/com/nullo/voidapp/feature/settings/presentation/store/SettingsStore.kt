package com.nullo.voidapp.feature.settings.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.utils.resources.UiText

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, SettingsStore.Label> {

    @Immutable
    data class State(
        val apiKey: String = "",
        val isApiKeySaved: Boolean = false,
        val appTheme: AppTheme = AppTheme.SYSTEM,
        val loading: Loading? = null,
        val dialog: Dialog? = null,
    ) {

        sealed interface Dialog {

            data object SignOutConfirmation : Dialog

            data class Error(val message: UiText) : Dialog
        }

        sealed interface Loading {

            data object ApiKey : Loading

            data object FullScreen : Loading
        }
    }

    sealed interface Intent {

        data object NavigateBack : Intent

        data class ChangeApiKey(val input: String) : Intent
        data object SubmitApiKey : Intent

        data class ChangeAppTheme(val theme: AppTheme) : Intent

        data object RequestSignOut : Intent
        data object ConfirmSignOut : Intent

        data object DismissDialog : Intent
    }

    sealed interface Label {

        data object NavigatedBack : Label

        data object SignedOut : Label
    }
}
