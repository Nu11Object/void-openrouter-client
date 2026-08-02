package com.nullo.voidapp.feature.settings.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.auth.domain.exception.AuthException
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.core.utils.resources.toUiText
import com.nullo.voidapp.feature.settings.domain.ChangeAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.ObserveAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.SignOutUseCase
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Dialog
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Loading
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStoreFactory.BootstrapperImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.exception_internal
import voidapp.core.utils.generated.resources.exception_invalid_api_key
import voidapp.core.utils.generated.resources.exception_secure_storage
import voidapp.core.utils.generated.resources.exception_validation_network
import voidapp.core.utils.generated.resources.exception_validation_unexpected
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val validateAndSaveApiKeyUseCase: ValidateAndSaveApiKeyUseCase,
    private val observeAppThemeUseCase: ObserveAppThemeUseCase,
    private val changeAppThemeUseCase: ChangeAppThemeUseCase,
    private val signOutUseCase: SignOutUseCase,
) : StoreFactory by storeFactory {

    fun create(): SettingsStore = object : SettingsStore,
        Store<SettingsStore.Intent, SettingsStore.State, SettingsStore.Label> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {

        data class ApiKeyChanged(val input: String) : Message
        data object SubmittingApiKey : Message
        data object ApiKeySubmitted : Message
        data object ApiKeySuccessDismissed : Message

        data class AppThemeChanged(val theme: AppTheme) : Message

        data object SignOutRequested : Message
        data object SignOutConfirmed : Message

        data class ErrorOccurred(val uiText: UiText) : Message

        data object DialogDismissed : Message
    }

    sealed interface Action {

        data class ChangeTheme(val theme: AppTheme) : Action
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeAppThemeUseCase()
                .onEach { theme -> dispatch(Action.ChangeTheme(theme)) }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Action, SettingsStore.State, Message, SettingsStore.Label>() {

        private var apiKeySuccessJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.ChangeTheme -> dispatch(Message.AppThemeChanged(action.theme))
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent) {
            when (intent) {
                is SettingsStore.Intent.ChangeApiKey -> {
                    apiKeySuccessJob?.cancel()
                    dispatch(Message.ApiKeyChanged(intent.input))
                }

                SettingsStore.Intent.SubmitApiKey -> {
                    apiKeySuccessJob?.cancel()
                    dispatch(Message.SubmittingApiKey)
                    scope.launch {
                        try {
                            validateAndSaveApiKeyUseCase(state().apiKey)
                            dispatch(Message.ApiKeySubmitted)

                            apiKeySuccessJob = scope.launch {
                                delay(2.seconds)
                                dispatch(Message.ApiKeySuccessDismissed)
                            }
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }

                is SettingsStore.Intent.ChangeAppTheme -> {
                    scope.launch {
                        try {
                            changeAppThemeUseCase(intent.theme)
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }

                SettingsStore.Intent.RequestSignOut -> dispatch(
                    Message.SignOutRequested
                )

                SettingsStore.Intent.ConfirmSignOut -> {
                    dispatch(Message.SignOutConfirmed)
                    scope.launch {
                        try {
                            signOutUseCase()
                            publish(SettingsStore.Label.SignedOut)
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }

                SettingsStore.Intent.DismissDialog -> dispatch(
                    Message.DialogDismissed
                )

                SettingsStore.Intent.NavigateBack -> publish(
                    SettingsStore.Label.NavigatedBack
                )
            }
        }

        private fun handleException(e: Exception) {
            if (e is CancellationException) throw e

            val uiText = when (e) {
                is AuthException.InvalidApiKeyException -> UiText(Res.string.exception_invalid_api_key)
                is AuthException.InternalServerException -> UiText(Res.string.exception_internal)
                is AuthException.UnexpectedStatusException -> UiText(Res.string.exception_validation_unexpected)
                is AuthException.ValidationNetworkException -> UiText(Res.string.exception_validation_network)
                is SecureStorageException -> UiText(Res.string.exception_secure_storage)
                else -> e.toUiText()
            }

            dispatch(Message.ErrorOccurred(uiText))
        }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Message> {

        override fun SettingsStore.State.reduce(msg: Message): SettingsStore.State {
            return when (msg) {
                is Message.ApiKeyChanged -> copy(apiKey = msg.input, isApiKeySaved = false)
                Message.SubmittingApiKey -> copy(loading = Loading.ApiKey, isApiKeySaved = false)
                Message.ApiKeySubmitted -> copy(apiKey = "", loading = null, isApiKeySaved = true)
                Message.ApiKeySuccessDismissed -> copy(isApiKeySaved = false)

                is Message.AppThemeChanged -> copy(appTheme = msg.theme)

                is Message.ErrorOccurred -> copy(dialog = Dialog.Error(msg.uiText), loading = null)
                Message.DialogDismissed -> copy(dialog = null)

                Message.SignOutRequested -> copy(dialog = Dialog.SignOutConfirmation)

                Message.SignOutConfirmed -> copy(dialog = null, loading = Loading.FullScreen)
            }
        }
    }
}
