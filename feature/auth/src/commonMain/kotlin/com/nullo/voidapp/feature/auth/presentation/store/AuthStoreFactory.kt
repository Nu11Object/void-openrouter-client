package com.nullo.voidapp.feature.auth.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.network.auth.exception.CodeExchangeException
import com.nullo.voidapp.core.network.auth.exception.InternalServerException
import com.nullo.voidapp.core.network.auth.exception.InvalidApiKeyException
import com.nullo.voidapp.core.network.auth.exception.UnexpectedStatusException
import com.nullo.voidapp.core.network.auth.exception.ValidationNetworkException
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.core.utils.resources.toUiText
import com.nullo.voidapp.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInViaOpenRouterUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInWithApiKeyUseCase
import com.nullo.voidapp.feature.auth.exception.ApiKeyException
import com.nullo.voidapp.feature.auth.exception.OAuthCancelledException
import com.nullo.voidapp.feature.auth.exception.OAuthException
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore.State.Completion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_code_exchange
import voidapp.feature.auth.generated.resources.exception_internal
import voidapp.feature.auth.generated.resources.exception_invalid_api_key
import voidapp.feature.auth.generated.resources.exception_secure_storage
import voidapp.feature.auth.generated.resources.exception_validation_network
import voidapp.feature.auth.generated.resources.exception_validation_unexpected

internal class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    private val signInViaOpenRouterUseCase: SignInViaOpenRouterUseCase,
    private val signInWithApiKeyUseCase: SignInWithApiKeyUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
) : StoreFactory by storeFactory {

    fun create(): AuthStore = object : AuthStore,
        Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {

        data object LaunchingOAuthFlow : Message

        data object OAuthCancelled : Message

        data class ApiKeyChanged(val input: String) : Message

        data object SubmittingApiKey : Message

        data class ErrorOccurred(val uiText: UiText) : Message

        data object ErrorDismissed : Message

        data class Authenticated(val completion: Completion) : Message
    }

    private sealed interface Action {

        data class FinishAuth(val completion: Completion) : Action
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            // If authed on first emission (api key exists) -> instant auth skip.
            // If authed later (first-time login) -> animate the transition.
            var isFirstEmission = true

            observeAuthStateUseCase()
                .onEach { hasKey ->
                    if (hasKey) {
                        val completion = if (isFirstEmission) {
                            Completion.Instant
                        } else {
                            Completion.Animated
                        }
                        dispatch(Action.FinishAuth(completion))
                    }
                    isFirstEmission = false
                }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Action, AuthStore.State, Message, Nothing>() {

        private var oAuthJob: Job? = null
        private var apiKeyAuthJob: Job? = null

        private fun handleException(e: Exception) {
            val finalException = when (e) {
                is InvalidApiKeyException -> ApiKeyException(
                    uiText = UiText(Res.string.exception_invalid_api_key),
                    cause = e
                )

                is InternalServerException -> ApiKeyException(
                    uiText = UiText(Res.string.exception_internal),
                    cause = e
                )

                is UnexpectedStatusException -> ApiKeyException(
                    uiText = UiText(Res.string.exception_validation_unexpected),
                    cause = e
                )

                is ValidationNetworkException -> ApiKeyException(
                    uiText = UiText(Res.string.exception_validation_network),
                    cause = e
                )

                is SecureStorageException -> ApiKeyException(
                    uiText = UiText(Res.string.exception_secure_storage),
                    cause = e
                )

                is CodeExchangeException -> OAuthException(
                    uiText = UiText(Res.string.exception_code_exchange),
                    cause = e
                )

                is OAuthCancelledException -> {
                    dispatch(Message.OAuthCancelled)
                    return
                }

                else -> e
            }

            dispatch(Message.ErrorOccurred(finalException.toUiText()))
        }

        override fun executeAction(action: Action) {
            when (action) {
                is Action.FinishAuth -> dispatch(Message.Authenticated(action.completion))
            }
        }

        override fun executeIntent(intent: AuthStore.Intent) {
            when (intent) {
                is AuthStore.Intent.ChangeApiKey -> {
                    dispatch(Message.ApiKeyChanged(intent.input))
                }

                AuthStore.Intent.DismissError -> {
                    dispatch(Message.ErrorDismissed)
                }

                AuthStore.Intent.SignInViaOpenRouter -> {
                    dispatch(Message.LaunchingOAuthFlow)
                    oAuthJob?.cancel()
                    oAuthJob = scope.launch {
                        try {
                            signInViaOpenRouterUseCase()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }

                AuthStore.Intent.CancelOAuth -> {
                    oAuthJob?.cancel()
                    oAuthJob = null
                    dispatch(Message.OAuthCancelled)
                }

                AuthStore.Intent.SubmitApiKey -> {
                    dispatch(Message.SubmittingApiKey)
                    apiKeyAuthJob?.cancel()
                    apiKeyAuthJob = scope.launch {
                        try {
                            signInWithApiKeyUseCase(state().apiKey)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<AuthStore.State, Message> {

        override fun AuthStore.State.reduce(msg: Message): AuthStore.State {
            return when (msg) {
                is Message.ApiKeyChanged -> copy(apiKey = msg.input)
                Message.ErrorDismissed -> copy(error = null, isLoading = false)
                is Message.ErrorOccurred -> copy(error = msg.uiText, isLoading = false)
                Message.LaunchingOAuthFlow -> copy(isLoading = true, isOAuthInProgress = true)
                Message.OAuthCancelled -> copy(isLoading = false, isOAuthInProgress = false)
                Message.SubmittingApiKey -> copy(isLoading = true)
                is Message.Authenticated -> copy(completion = msg.completion)
            }
        }
    }
}
