package com.nullo.voidapp.feature.catalog.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.data.catalog.domain.ObserveChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObserveImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.RefreshCatalogUseCase
import com.nullo.voidapp.core.data.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.core.data.catalog.domain.UseModelUseCase
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.kotlin.debouncedQuery
import com.nullo.voidapp.core.utils.kotlin.filterWithQuery
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.core.utils.resources.toUiText
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import voidapp.core.utils.generated.resources.exception_internal
import voidapp.core.utils.generated.resources.exception_network
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.message_catalog_refreshed
import voidapp.feature.catalog.generated.resources.message_using_model
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import voidapp.core.utils.generated.resources.Res as CoreRes

internal class CatalogStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeChatModelsUseCase: ObserveChatModelsUseCase,
    private val observeImageModelsUseCase: ObserveImageModelsUseCase,
    private val refreshCatalogUseCase: RefreshCatalogUseCase,
    private val setPinStatusUseCase: SetPinStatusUseCase,
    private val useModelUseCase: UseModelUseCase,
) : StoreFactory by storeFactory {

    fun create(): CatalogStore {
        // Internal state flow to sync search queries between Executor and Bootstrapper.
        // State.searchQuery is used for UI only!
        val searchQuery = MutableStateFlow("")

        return object : CatalogStore,
            Store<CatalogStore.Intent, State, CatalogStore.Label> by storeFactory.create(
                name = "CatalogStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(searchQuery),
                executorFactory = { ExecutorImpl(searchQuery) },
                reducer = ReducerImpl
            ) {}
    }

    private sealed interface Message {

        data class SearchQueryChanged(val query: String) : Message
        data object SearchQueryCleared : Message

        data class CatalogTabChanged(val tab: State.CatalogTab) : Message
        data class VerboseInfoToggled(val enabled: Boolean) : Message
        data object CatalogRefreshStarted : Message
        data object CatalogRefreshFinished : Message

        data class ModelsUpdated(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
            val isDbEmpty: Boolean,
        ) : Message

        data class ErrorOccurred(val text: UiText) : Message
        data class MessageReceived(val text: UiText) : Message
        data class MessageDismissalTimerChanged(val newValue: Int) : Message

        data object ErrorDismissed : Message
        data object MessageDismissed : Message
    }

    private sealed interface Action {

        data class UpdateModels(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
            val isDbEmpty: Boolean
        ) : Action

        data object AutoRefreshCatalog : Action
    }

    private inner class BootstrapperImpl(
        private val searchQuery: Flow<String>
    ) : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            var isFirstEmission = true

            combine(
                observeChatModelsUseCase(),
                observeImageModelsUseCase(),
                searchQuery.debouncedQuery(SEARCH_QUERY_DEBOUNCE)
            ) { chatModels, imageModels, query ->
                val isDbEmpty = chatModels.isEmpty() && imageModels.isEmpty()

                val filteredChat = chatModels.filterWithQuery(
                    query = query,
                    filterBy = { it.name }
                )

                val filteredImage = imageModels.filterWithQuery(
                    query = query,
                    filterBy = { it.name }
                )

                Triple(filteredChat, filteredImage, isDbEmpty)
            }
                .onEach { (chatModels, imageModels, isDbEmpty) ->
                    dispatch(Action.UpdateModels(chatModels, imageModels, isDbEmpty))

                    if (isFirstEmission) {
                        isFirstEmission = false
                        if (isDbEmpty) {
                            dispatch(Action.AutoRefreshCatalog)
                        }
                    }
                }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl(
        private val searchQuery: MutableStateFlow<String>
    ) : CoroutineExecutor<CatalogStore.Intent, Action, State, Message, CatalogStore.Label>() {

        private var useModelJob: Job? = null
        private var dismissMessageJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.UpdateModels -> dispatch(
                    Message.ModelsUpdated(
                        chatModels = action.chatModels,
                        imageModels = action.imageModels,
                        isDbEmpty = action.isDbEmpty
                    )
                )

                is Action.AutoRefreshCatalog -> executeIntent(CatalogStore.Intent.RefreshCatalog)
            }
        }

        override fun executeIntent(intent: CatalogStore.Intent) {
            when (intent) {
                is CatalogStore.Intent.ChangeCatalogTab -> dispatch(
                    Message.CatalogTabChanged(intent.tab)
                )

                is CatalogStore.Intent.ChangeSearchQuery -> {
                    dispatch(Message.SearchQueryChanged(intent.query))
                    searchQuery.update { intent.query }
                }

                is CatalogStore.Intent.ClearSearchQuery -> {
                    dispatch(Message.SearchQueryCleared)
                    searchQuery.update { "" }
                }

                CatalogStore.Intent.RefreshCatalog -> {
                    dispatch(Message.CatalogRefreshStarted)
                    scope.launch {
                        try {
                            refreshCatalogUseCase()
                            dispatch(Message.CatalogRefreshFinished)
                            showMessage(UiText(Res.string.message_catalog_refreshed))
                        } catch (e: Exception) {
                            handleException(e)
                        }
                    }
                }

                is CatalogStore.Intent.SetPinState -> scope.launch {
                    setPinStatusUseCase(
                        modelId = intent.modelId,
                        modelType = intent.modelType,
                        pinned = intent.pinned,
                    )
                }

                is CatalogStore.Intent.UseModel -> {
                    useModelJob?.cancel()
                    useModelJob = scope.launch {
                        val model = intent.modelRef
                        useModelUseCase(model)
                        showMessage(UiText(Res.string.message_using_model, model.id))
                    }
                }

                CatalogStore.Intent.DismissMessage -> {
                    dismissMessageJob?.cancel()
                    dispatch(Message.MessageDismissed)
                }

                is CatalogStore.Intent.ToggleVerboseInfo -> dispatch(
                    Message.VerboseInfoToggled(intent.verbose)
                )

                CatalogStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
                CatalogStore.Intent.NavigateBack -> publish(CatalogStore.Label.NavigatedBack)
            }
        }

        private fun showMessage(uiText: UiText) {
            dismissMessageJob?.cancel()
            dispatch(Message.MessageReceived(uiText))

            dismissMessageJob = scope.launch {
                repeat(DISMISSAL_MESSAGE_SECONDS) { elapsedSeconds ->
                    dispatch(
                        Message.MessageDismissalTimerChanged(
                            DISMISSAL_MESSAGE_SECONDS - elapsedSeconds
                        )
                    )
                    delay(1.seconds)
                }

                dispatch(Message.MessageDismissed)
            }
        }

        private fun handleException(e: Exception) {
            if (e is CancellationException) throw e

            val uiText = when (e) {
                is InternalServerException -> UiText(CoreRes.string.exception_internal)
                is NetworkException -> UiText(CoreRes.string.exception_network)
                else -> e.toUiText()
            }

            dispatch(Message.ErrorOccurred(uiText))
        }
    }

    private object ReducerImpl : Reducer<State, Message> {

        override fun State.reduce(msg: Message): State {
            return when (msg) {
                Message.CatalogRefreshStarted -> copy(isLoading = true)
                Message.CatalogRefreshFinished -> copy(isLoading = false)

                is Message.CatalogTabChanged -> copy(selectedTab = msg.tab)

                is Message.ModelsUpdated -> copy(
                    chatModels = msg.chatModels,
                    imageModels = msg.imageModels,
                    isDbEmpty = msg.isDbEmpty
                )

                is Message.MessageReceived -> copy(message = msg.text)
                Message.MessageDismissed -> copy(message = null)
                is Message.MessageDismissalTimerChanged -> copy(
                    messageDismissalTimer = msg.newValue
                )

                is Message.ErrorOccurred -> copy(error = msg.text, isLoading = false)
                Message.ErrorDismissed -> copy(error = null)

                is Message.SearchQueryChanged -> copy(searchQuery = msg.query)
                is Message.SearchQueryCleared -> copy(searchQuery = "")
                is Message.VerboseInfoToggled -> copy(showVerboseInfo = msg.enabled)
            }
        }
    }

    companion object {

        private const val DISMISSAL_MESSAGE_SECONDS = 5
        private val SEARCH_QUERY_DEBOUNCE = 300.milliseconds
    }
}
