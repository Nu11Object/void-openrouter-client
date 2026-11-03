package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.core.data.catalog.domain.UseModelUseCase
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.kotlin.debouncedQuery
import com.nullo.voidapp.core.utils.kotlin.filterWithQuery
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.State
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ModelSelectorStoreFactory(
    private val storeFactory: StoreFactory,
    private val observePinnedChatModelsUseCase: ObservePinnedChatModelsUseCase,
    private val observePinnedImageModelsUseCase: ObservePinnedImageModelsUseCase,
    private val setPinStatusUseCase: SetPinStatusUseCase,
    private val useModelUseCase: UseModelUseCase,
) : StoreFactory by storeFactory {

    fun create(): ModelSelectorStore {
        // Search query from ModelSelectorStore.State is used for UI only!
        val modelSearchQuery = MutableStateFlow("")

        return object :
            ModelSelectorStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "ModelSelectorStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(modelSearchQuery),
                executorFactory = { ExecutorImpl(modelSearchQuery) },
                reducer = ReducerImpl
            ) {}
    }

    private sealed interface Msg {

        data object ModelSelectorOpened : Msg
        data object ModelSelectorClosed : Msg

        data class ModelSelectorTabChanged(val tab: State.SelectorTab) : Msg
        data class ModelSearchQueryChanged(val query: String) : Msg

        data class PinnedModelsUpdated(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
        ) : Msg
    }

    private sealed interface Action {

        data class UpdatePinnedModels(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
        ) : Action
    }

    private inner class BootstrapperImpl(private val modelSearchQuery: Flow<String>) :
        CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observePinnedModels()
        }

        private fun observePinnedModels() {
            combine(
                observePinnedChatModelsUseCase(),
                observePinnedImageModelsUseCase(),
                modelSearchQuery.debouncedQuery(SEARCH_QUERY_DEBOUNCE)
            ) { chatModels, imageModels, query ->
                val filteredChat = chatModels.filterWithQuery(
                    query = query,
                    filterBy = { it.name }
                )
                val filteredImage = imageModels.filterWithQuery(
                    query = query,
                    filterBy = { it.name }
                )
                filteredChat to filteredImage
            }
                .onEach { (filteredChat, filteredImage) ->
                    dispatch(Action.UpdatePinnedModels(filteredChat, filteredImage))
                }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl(private val modelSearchQuery: MutableStateFlow<String>) :
        CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.UpdatePinnedModels -> dispatch(
                    Msg.PinnedModelsUpdated(action.chatModels, action.imageModels)
                )
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.OpenModelSelector -> dispatch(Msg.ModelSelectorOpened)

                Intent.CloseModelSelector -> dispatch(Msg.ModelSelectorClosed)

                is Intent.ChangeModelSelectorTab -> dispatch(
                    Msg.ModelSelectorTabChanged(intent.tab)
                )

                is Intent.ChangeModelSearchQuery -> {
                    dispatch(Msg.ModelSearchQueryChanged(intent.query))
                    modelSearchQuery.update { intent.query }
                }

                is Intent.SelectModel -> scope.launch {
                    useModelUseCase(intent.activeModelRef)
                }

                is Intent.UnpinModel -> scope.launch {
                    setPinStatusUseCase(intent.modelId, intent.modelType, pinned = false)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            Msg.ModelSelectorOpened -> copy(
                isModelSelectorOpen = true
            )

            Msg.ModelSelectorClosed -> copy(
                isModelSelectorOpen = false
            )

            is Msg.ModelSelectorTabChanged -> copy(
                modelSelectorTab = msg.tab
            )

            is Msg.ModelSearchQueryChanged -> copy(
                modelSearchQuery = msg.query
            )

            is Msg.PinnedModelsUpdated -> copy(
                pinnedChatModels = msg.chatModels,
                pinnedImageModels = msg.imageModels
            )
        }
    }

    companion object {

        private val SEARCH_QUERY_DEBOUNCE = 300.milliseconds
    }
}
