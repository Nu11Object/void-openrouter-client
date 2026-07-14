package com.nullo.voidapp.feature.auth.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.utils.resources.UiText

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    @Immutable
    data class State(
        val apiKey: String = "",
        val isLoading: Boolean = false,
        val isOAuthInProgress: Boolean = false,
        val error: UiText? = null,
        val completion: Completion? = null
    ) {

        /** Determines how the UI handles successful auth. */
        sealed interface Completion {

            /** Instantly skips the screen (logged in before). */
            data object Instant : Completion

            /** Plays the exit animation first (first-time login). */
            data object Animated : Completion
        }
    }

    sealed interface Intent {

        data object SignInViaOpenRouter : Intent

        data object CancelOAuth : Intent

        data class ChangeApiKey(val input: String) : Intent

        data object SubmitApiKey : Intent

        data object DismissError : Intent
    }
}
