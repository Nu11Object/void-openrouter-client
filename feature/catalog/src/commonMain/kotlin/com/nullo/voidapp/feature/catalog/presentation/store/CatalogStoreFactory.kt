package com.nullo.voidapp.feature.catalog.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.data.catalog.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.service.exception.NetworkException
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.core.utils.resources.toUiText
import com.nullo.voidapp.feature.catalog.domain.ObserveChatModelsUseCase
import com.nullo.voidapp.feature.catalog.domain.ObserveImageModelsUseCase
import com.nullo.voidapp.feature.catalog.domain.RefreshCatalogUseCase
import com.nullo.voidapp.feature.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.feature.catalog.domain.UseModelUseCase
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
        data class CatalogTabChanged(val tab: State.CatalogTab) : Message
        data object VerboseInfoToggled : Message
        data object CatalogRefreshStarted : Message
        data object CatalogRefreshFinished : Message

        data class ChatModelsUpdated(val models: List<ChatModel>) : Message
        data class ImageModelsUpdated(val models: List<ImageModel>) : Message

        data class ErrorOccurred(val uiText: UiText) : Message
        data class MessageReceived(val uiText: UiText) : Message

        data object ErrorDismissed : Message
        data object MessageDismissed : Message
    }

    private sealed interface Action {

        data class UpdateChatModelsList(val models: List<ChatModel>) : Action
        data class UpdateImageModelsList(val models: List<ImageModel>) : Action
    }

    private inner class BootstrapperImpl(
        private val searchQuery: Flow<String>
    ) : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeModelsWithQuery(
                modelSource = observeChatModelsUseCase(),
                searchQuery = searchQuery,
                filterBy = { it.name },
                onEach = { dispatch(Action.UpdateChatModelsList(it)) },
                scope = scope
            )

            observeModelsWithQuery(
                modelSource = observeImageModelsUseCase(),
                searchQuery = searchQuery,
                filterBy = { it.name },
                onEach = { dispatch(Action.UpdateImageModelsList(it)) },
                scope = scope
            )
        }
    }

    private inner class ExecutorImpl(
        private val searchQuery: MutableStateFlow<String>
    ) : CoroutineExecutor<CatalogStore.Intent, Action, State, Message, CatalogStore.Label>() {

        private var useModelJob: Job? = null
        private var dismissMessageJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.UpdateChatModelsList -> dispatch(
                    Message.ChatModelsUpdated(action.models)
                )

                is Action.UpdateImageModelsList -> dispatch(
                    Message.ImageModelsUpdated(action.models)
                )
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

                CatalogStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
                CatalogStore.Intent.NavigateBack -> publish(CatalogStore.Label.NavigatedBack)
                CatalogStore.Intent.ToggleVerboseInfo -> dispatch(Message.VerboseInfoToggled)
            }
        }

        private fun showMessage(uiText: UiText) {
            dismissMessageJob?.cancel()
            dispatch(Message.MessageReceived(uiText))

            dismissMessageJob = scope.launch {
                delay(DISMISS_MESSAGE_DELAY_MS.milliseconds)
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
                is Message.ChatModelsUpdated -> copy(chatModels = msg.models)
                is Message.ImageModelsUpdated -> copy(imageModels = msg.models)

                is Message.MessageReceived -> copy(message = msg.uiText)
                Message.MessageDismissed -> copy(message = null)
                is Message.ErrorOccurred -> copy(error = msg.uiText, isLoading = false)
                Message.ErrorDismissed -> copy(error = null)

                is Message.SearchQueryChanged -> copy(searchQuery = msg.query)
                Message.VerboseInfoToggled -> copy(showVerboseInfo = !showVerboseInfo)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private inline fun <T> observeModelsWithQuery(
        modelSource: Flow<List<T>>,
        searchQuery: Flow<String>,
        crossinline filterBy: (T) -> String,
        crossinline onEach: (filtered: List<T>) -> Unit,
        scope: CoroutineScope,
    ) {
        val debouncedQuery = searchQuery.debounce { query ->
            if (query.isEmpty()) {
                0.milliseconds
            } else {
                SEARCH_QUERY_DEBOUNCE_MS.milliseconds
            }
        }

        combine(
            modelSource,
            debouncedQuery
        ) { models, query ->
            if (query.isBlank()) {
                models
            } else {
                models.filter { filterBy(it).contains(query, ignoreCase = true) }
            }
        }
            .onEach { onEach(it) }
            .launchIn(scope)
    }

    companion object {

        private const val DISMISS_MESSAGE_DELAY_MS = 5_000
        private const val SEARCH_QUERY_DEBOUNCE_MS = 300
    }
}
