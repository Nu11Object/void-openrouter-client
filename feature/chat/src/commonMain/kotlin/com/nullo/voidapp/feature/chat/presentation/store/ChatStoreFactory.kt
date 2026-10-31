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
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.kotlin.debouncedQuery
import com.nullo.voidapp.core.utils.kotlin.filterWithQuery
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.domain.DeleteAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.DeleteConversationUseCase
import com.nullo.voidapp.feature.chat.domain.DismissErrorMessageUseCase
import com.nullo.voidapp.feature.chat.domain.FindConversationIdsByTextUseCase
import com.nullo.voidapp.feature.chat.domain.ImportAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationsUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveMessagesUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.RegenerateLastResponseUseCase
import com.nullo.voidapp.feature.chat.domain.RenameConversationUseCase
import com.nullo.voidapp.feature.chat.domain.SendPromptUseCase
import com.nullo.voidapp.feature.chat.domain.SetThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.SetWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.StartNewConversationUseCase
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.State
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.error_blank_conversation_name
import voidapp.feature.chat.generated.resources.error_cannot_access_conversation
import voidapp.feature.chat.generated.resources.error_cannot_rename_conversation
import voidapp.feature.chat.generated.resources.error_empty_prompt_on_image
import voidapp.feature.chat.generated.resources.error_empty_request
import voidapp.feature.chat.generated.resources.error_model_no_image_input
import voidapp.feature.chat.generated.resources.error_model_not_selected
import voidapp.feature.chat.generated.resources.error_no_active_conversation
import voidapp.feature.chat.generated.resources.error_some_attachments_import_failed
import voidapp.feature.chat.generated.resources.template_error_max_attachments_reached

internal class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val observePinnedChatModelsUseCase: ObservePinnedChatModelsUseCase,
    private val observePinnedImageModelsUseCase: ObservePinnedImageModelsUseCase,
    private val observeConversationsUseCase: ObserveConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val findConversationIdsByTextUseCase: FindConversationIdsByTextUseCase,
    private val renameConversationUseCase: RenameConversationUseCase,
    private val observeActiveModelUseCase: ObserveActiveModelUseCase,
    private val observeChatModelByIdUseCase: ObserveChatModelByIdUseCase,
    private val observeImageModelByIdUseCase: ObserveImageModelByIdUseCase,
    private val observeThinkingEnabledUseCase: ObserveThinkingEnabledUseCase,
    private val observeWebSearchEnabledUseCase: ObserveWebSearchEnabledUseCase,
    private val dismissErrorMessageUseCase: DismissErrorMessageUseCase,
    private val observeConversationByIdUseCase: ObserveConversationByIdUseCase,
    private val regenerateLastResponseUseCase: RegenerateLastResponseUseCase,
    private val startNewConversationUseCase: StartNewConversationUseCase,
    private val setThinkingEnabledUseCase: SetThinkingEnabledUseCase,
    private val setWebSearchEnabledUseCase: SetWebSearchEnabledUseCase,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val setPinStatusUseCase: SetPinStatusUseCase,
    private val sendPromptUseCase: SendPromptUseCase,
    private val useModelUseCase: UseModelUseCase,
    private val importAttachmentUseCase: ImportAttachmentUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase,
) : StoreFactory by storeFactory {

    fun create(): ChatStore {
        // Any search queries from ChatStore.State is used for UI only!
        val conversationSearchQuery = MutableStateFlow("")
        val modelSearchQuery = MutableStateFlow("")
        val conversationId = MutableStateFlow<Long?>(null)

        return object :
            ChatStore,
            Store<Intent, State, ChatStore.Label> by storeFactory.create(
                name = "ChatStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(
                    conversationSearchQuery,
                    modelSearchQuery,
                    conversationId
                ),
                executorFactory = {
                    ExecutorImpl(
                        conversationSearchQuery,
                        modelSearchQuery,
                        conversationId
                    )
                },
                reducer = ReducerImpl
            ) {}
    }

    private sealed interface Msg {
        data object ModelSelectorOpened : Msg
        data object ModelSelectorClosed : Msg
        data class ModelSelectorTabChanged(val tab: State.SelectorTab) : Msg
        data class ModelSearchQueryChanged(val query: String) : Msg
        data class ActiveModelChanged(
            val modelType: ModelType,
            val modelName: String,
            val supportsImageInput: Boolean,
        ) : Msg

        data object ModelSelectionConfirmationShowed : Msg
        data object ModelSelectionConfirmationHidden : Msg

        data class ConversationsUpdated(val conversations: List<Conversation>) : Msg
        data class MessagesUpdated(val messages: List<Message>) : Msg

        data class PinnedModelsUpdated(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
        ) : Msg

        data object TuningPanelOpened : Msg
        data object TuningPanelClosed : Msg

        data class ThinkingStateChanged(val enabled: Boolean) : Msg
        data class WebSearchStateChanged(val enabled: Boolean) : Msg

        data class PromptChanged(val prompt: String) : Msg

        data class ErrorOccurred(val text: UiText) : Msg
        data object ErrorDialogDismissed : Msg

        data object NewConversationStarted : Msg
        data class ConversationOpened(val conversation: Conversation) : Msg
        data class ConversationSearchQueryChanged(val query: String) : Msg

        data class RenameConversationDialogOpened(val conversationId: Long) : Msg
        data object RenameConversationDialogClosed : Msg
        data class ConversationNameInputChanged(val input: String) : Msg

        data object AttachmentsImportStarted : Msg
        data class AttachmentsAdded(val attachments: List<ImageAttachment>) : Msg
        data class AttachmentsRestored(val attachments: List<ImageAttachment>) : Msg
        data object AttachmentsCleared : Msg
        data class AttachmentRemoved(val attachment: ImageAttachment) : Msg
    }

    private sealed interface Action {

        data class UpdateActiveModel(val activeModelRef: ActiveModelRef?) : Action
        data class UpdateActiveConversation(val conversation: Conversation?) : Action

        data class UpdateConversations(val conversations: List<Conversation>) : Action
        data class UpdateMessages(val messages: List<Message>) : Action
        data class UpdatePinnedModels(
            val chatModels: List<ChatModel>,
            val imageModels: List<ImageModel>,
        ) : Action

        data class UpdateThinkingEnabled(val enabled: Boolean) : Action
        data class UpdateWebSearchEnabled(val enabled: Boolean) : Action
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private inner class BootstrapperImpl(
        private val conversationSearchQuery: Flow<String>,
        private val modelSearchQuery: Flow<String>,
        private val conversationId: Flow<Long?>,
    ) : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeActiveModel()
            observePinnedModels()
            observeConversations()
            observeActiveConversation()
            observeMessages()
            observeThinkingEnabled()
            observeWebSearchEnabled()
        }

        private fun observeActiveModel() {
            observeActiveModelUseCase()
                .onEach { dispatch(Action.UpdateActiveModel(it)) }
                .launchIn(scope)
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

        private fun observeConversations() {
            val matchedIds = conversationSearchQuery
                .debouncedQuery(SEARCH_QUERY_DEBOUNCE)
                .flatMapLatest { query ->
                    flow { emit(query to findConversationIdsByTextUseCase(query)) }
                }

            combine(observeConversationsUseCase(), matchedIds) { conversations, (query, ids) ->
                val titleMatches = conversations
                    .filterWithQuery(query = query, filterBy = { it.title.orEmpty() })
                    .mapTo(HashSet()) { it.id }
                conversations.filter { it.id in titleMatches || it.id in ids }
            }
                .onEach { dispatch(Action.UpdateConversations(it)) }
                .launchIn(scope)
        }

        private fun observeActiveConversation() {
            conversationId
                .flatMapLatest { id ->
                    if (id == null) {
                        flowOf(null)
                    } else {
                        observeConversationByIdUseCase(id)
                    }
                }
                .onEach { dispatch(Action.UpdateActiveConversation(it)) }
                .launchIn(scope)
        }

        private fun observeMessages() {
            conversationId
                .flatMapLatest { id ->
                    if (id == null) {
                        flowOf(emptyList())
                    } else {
                        observeMessagesUseCase(id)
                    }
                }
                .onEach { dispatch(Action.UpdateMessages(it)) }
                .launchIn(scope)
        }

        private fun observeThinkingEnabled() {
            observeThinkingEnabledUseCase()
                .onEach { dispatch(Action.UpdateThinkingEnabled(it)) }
                .launchIn(scope)
        }

        private fun observeWebSearchEnabled() {
            observeWebSearchEnabledUseCase()
                .onEach { dispatch(Action.UpdateWebSearchEnabled(it)) }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl(
        private val conversationSearchQuery: MutableStateFlow<String>,
        private val modelSearchQuery: MutableStateFlow<String>,
        private val conversationId: MutableStateFlow<Long?>,
    ) : CoroutineExecutor<Intent, Action, State, Msg, ChatStore.Label>() {
        private var activeModelRef: ActiveModelRef? = null
        private var activeModelNameJob: Job? = null

        private var completionGenerationJob: Job? = null
        private var modelSelectedIndicatorJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.UpdateActiveModel -> {
                    val modelRef = action.activeModelRef
                    activeModelRef = modelRef
                    activeModelNameJob?.cancel()
                    activeModelNameJob = when (modelRef) {
                        is ActiveModelRef.Chat -> observeChatModelByIdUseCase(modelRef.id)
                            .filterNotNull()
                            .onEach { dispatch(Msg.ActiveModelChanged(ModelType.CHAT, it.name, it.supportsVision)) }
                            .launchIn(scope)

                        is ActiveModelRef.Image -> observeImageModelByIdUseCase(modelRef.id)
                            .filterNotNull()
                            .onEach {
                                dispatch(Msg.ActiveModelChanged(ModelType.IMAGE, it.name, it.supportsInputReferences))
                            }
                            .launchIn(scope)

                        null -> return
                    }
                }

                is Action.UpdateConversations -> dispatch(
                    Msg.ConversationsUpdated(action.conversations)
                )

                is Action.UpdatePinnedModels -> dispatch(
                    Msg.PinnedModelsUpdated(action.chatModels, action.imageModels)
                )

                is Action.UpdateMessages -> dispatch(
                    Msg.MessagesUpdated(action.messages)
                )

                is Action.UpdateActiveConversation -> {
                    if (action.conversation == null) {
                        dispatch(Msg.NewConversationStarted)
                        conversationId.value = null
                    } else {
                        dispatch(Msg.ConversationOpened(action.conversation))
                    }
                }

                is Action.UpdateThinkingEnabled -> dispatch(
                    Msg.ThinkingStateChanged(action.enabled)
                )

                is Action.UpdateWebSearchEnabled -> dispatch(
                    Msg.WebSearchStateChanged(action.enabled)
                )
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SelectModel -> scope.launch {
                    useModelUseCase(intent.activeModelRef)
                    dispatch(Msg.ModelSelectionConfirmationShowed)

                    modelSelectedIndicatorJob?.cancel()
                    modelSelectedIndicatorJob = launch {
                        delay(MODEL_SELECTED_INDICATOR_DURATION)
                        dispatch(Msg.ModelSelectionConfirmationHidden)
                    }
                }

                is Intent.ChangeConversationSearchQuery -> {
                    dispatch(Msg.ConversationSearchQueryChanged(intent.query))
                    conversationSearchQuery.update { intent.query }
                }

                is Intent.ChangeModelSearchQuery -> {
                    dispatch(Msg.ModelSearchQueryChanged(intent.query))
                    modelSearchQuery.update { intent.query }
                }

                is Intent.ChangeModelSelectorTab -> dispatch(
                    Msg.ModelSelectorTabChanged(intent.tab)
                )

                Intent.CloseModelSelector -> dispatch(Msg.ModelSelectorClosed)

                Intent.CloseTuningPanel -> dispatch(Msg.TuningPanelClosed)

                Intent.DismissErrorDialog -> dispatch(Msg.ErrorDialogDismissed)

                is Intent.DismissChatError -> scope.launch {
                    dismissErrorMessageUseCase(intent.messageId)
                }

                is Intent.OpenConversation -> {
                    if (intent.conversationId == conversationId.value) return

                    completionGenerationJob?.cancel()
                    conversationId.value = intent.conversationId
                }

                Intent.RegenerateLastResponse -> {
                    if (completionGenerationJob?.isActive == true) return

                    val conversationId = conversationId.value ?: run {
                        dispatchError(UiText(Res.string.error_no_active_conversation))
                        return
                    }
                    val activeModel = activeModelRef ?: run {
                        dispatchError(UiText(Res.string.error_model_not_selected))
                        return
                    }

                    completionGenerationJob = scope.launch {
                        try {
                            regenerateLastResponseUseCase(conversationId, activeModel)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            dispatchError(UiText(CoreRes.string.error_unknown))
                        }
                    }
                }

                Intent.SendPrompt -> {
                    val state = state()
                    if (completionGenerationJob?.isActive == true || state.isImportingAttachments) return

                    val activeModel = activeModelRef ?: run {
                        dispatchError(UiText(Res.string.error_model_not_selected))
                        return
                    }
                    val prompt = state.prompt.trim()
                    val images = state.promptImages

                    val error = when {
                        prompt.isBlank() && images.isEmpty() -> {
                            UiText(Res.string.error_empty_request)
                        }

                        activeModel is ActiveModelRef.Image && prompt.isBlank() -> {
                            UiText(Res.string.error_empty_prompt_on_image)
                        }

                        images.isNotEmpty() && !state.supportsImageInput -> {
                            UiText(Res.string.error_model_no_image_input)
                        }

                        else -> null
                    }
                    error?.let {
                        dispatchError(it)
                        return
                    }

                    dispatch(Msg.PromptChanged(""))
                    dispatch(Msg.AttachmentsCleared)

                    completionGenerationJob = scope.launch {
                        try {
                            val conversationId = conversationId.value
                                ?: startNewConversationUseCase(prompt).also { conversationId.value = it }

                            sendPromptUseCase(conversationId, prompt, images, activeModel)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            dispatchError(UiText(CoreRes.string.error_unknown))
                            dispatch(Msg.PromptChanged(prompt))
                            dispatch(Msg.AttachmentsRestored(images))
                        }
                    }
                }

                Intent.ToggleThinking -> scope.launch {
                    val enabled = !state().thinkingEnabled
                    setThinkingEnabledUseCase(enabled)
                }

                Intent.ToggleWebSearch -> scope.launch {
                    val enabled = !state().webSearchEnabled
                    setWebSearchEnabledUseCase(enabled)
                }

                is Intent.UnpinModel -> scope.launch {
                    setPinStatusUseCase(intent.modelId, intent.modelType, pinned = false)
                }

                is Intent.ChangePrompt -> dispatch(Msg.PromptChanged(intent.prompt))

                Intent.OpenModelSelector -> dispatch(Msg.ModelSelectorOpened)

                Intent.StartNewConversation -> {
                    completionGenerationJob?.cancel()
                    conversationId.value = null
                }

                Intent.OpenTuningPanel -> {
                    dispatch(Msg.ErrorOccurred(UiText.Static("Not implemented yet")))
                }

                Intent.OpenAppSettings -> publish(ChatStore.Label.NavigatedToAppSettings)

                Intent.OpenFullModelCatalog -> publish(ChatStore.Label.NavigatedToModelCatalog)

                Intent.StopGeneration -> {
                    completionGenerationJob?.cancel()
                }

                is Intent.DeleteConversation -> scope.launch {
                    deleteConversationUseCase(intent.conversationId)
                }

                is Intent.OpenRenameConversationDialog -> {
                    dispatch(Msg.RenameConversationDialogOpened(intent.conversationId))
                }

                Intent.CloseRenameConversationDialog -> {
                    dispatch(Msg.RenameConversationDialogClosed)
                }

                is Intent.ChangeConversationNameInput -> {
                    dispatch(Msg.ConversationNameInputChanged(intent.input))
                }

                is Intent.ConfirmConversationRename -> scope.launch {
                    val state = state()
                    val id = state.renamingConversationId ?: run {
                        dispatchError(UiText(Res.string.error_cannot_access_conversation))
                        return@launch
                    }
                    val newTitle = state.renamingConversationInput.ifBlank {
                        dispatchError(UiText(Res.string.error_blank_conversation_name))
                        return@launch
                    }

                    try {
                        renameConversationUseCase(id, newTitle)
                        dispatch(Msg.RenameConversationDialogClosed)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        dispatchError(UiText(Res.string.error_cannot_rename_conversation))
                    }
                }

                is Intent.AttachImages -> scope.launch {
                    val remaining = State.MAX_ATTACHMENTS - state().promptImages.size
                    if (intent.files.size > remaining) {
                        dispatchError(UiText(Res.string.template_error_max_attachments_reached, State.MAX_ATTACHMENTS))
                    }
                    if (remaining <= 0) return@launch

                    dispatch(Msg.AttachmentsImportStarted)
                    var hasFailures = false
                    val imported = intent.files
                        .take(remaining)
                        .mapNotNull { file ->
                            try {
                                importAttachmentUseCase(file)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                hasFailures = true
                                null
                            }
                        }
                    if (hasFailures) dispatchError(UiText(Res.string.error_some_attachments_import_failed))
                    dispatch(Msg.AttachmentsAdded(imported))
                }

                is Intent.RemoveImageAttachment -> scope.launch {
                    dispatch(Msg.AttachmentRemoved(intent.attachment))
                    deleteAttachmentUseCase(intent.attachment)
                }
            }
        }

        private fun dispatchError(text: UiText) {
            dispatch(Msg.ErrorOccurred(text))
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.ActiveModelChanged -> copy(
                activeModelName = msg.modelName,
                modelType = msg.modelType,
                supportsImageInput = msg.supportsImageInput
            )

            is Msg.ConversationOpened -> copy(
                activeConversation = msg.conversation
            )

            is Msg.ConversationSearchQueryChanged -> copy(
                conversationSearchQuery = msg.query
            )

            is Msg.ConversationsUpdated -> copy(
                conversations = msg.conversations
            )

            Msg.ErrorDialogDismissed -> copy(
                errorDialogText = null
            )

            is Msg.ErrorOccurred -> copy(
                errorDialogText = msg.text
            )

            is Msg.ModelSearchQueryChanged -> copy(
                modelSearchQuery = msg.query
            )

            Msg.ModelSelectionConfirmationHidden -> copy(
                isModelSelectionConfirmationShown = false
            )

            Msg.ModelSelectionConfirmationShowed -> copy(
                isModelSelectionConfirmationShown = true
            )

            Msg.ModelSelectorClosed -> copy(
                isModelSelectorOpen = false
            )

            Msg.ModelSelectorOpened -> copy(
                isModelSelectorOpen = true
            )

            is Msg.ModelSelectorTabChanged -> copy(
                modelSelectorTab = msg.tab
            )

            is Msg.PinnedModelsUpdated -> copy(
                pinnedChatModels = msg.chatModels,
                pinnedImageModels = msg.imageModels
            )

            is Msg.PromptChanged -> copy(
                prompt = msg.prompt
            )

            Msg.NewConversationStarted -> copy(
                activeConversation = null,
                messages = emptyList()
            )

            is Msg.ThinkingStateChanged -> copy(
                thinkingEnabled = msg.enabled
            )

            Msg.TuningPanelClosed -> copy(
                isTuningPanelOpen = false
            )

            Msg.TuningPanelOpened -> copy(
                isTuningPanelOpen = true
            )

            is Msg.WebSearchStateChanged -> copy(
                webSearchEnabled = msg.enabled
            )

            is Msg.MessagesUpdated -> copy(
                messages = msg.messages
            )

            is Msg.ConversationNameInputChanged -> copy(
                renamingConversationInput = msg.input
            )

            Msg.RenameConversationDialogClosed -> copy(
                renamingConversationId = null,
                renamingConversationInput = ""
            )

            is Msg.RenameConversationDialogOpened -> copy(
                renamingConversationId = msg.conversationId
            )

            Msg.AttachmentsImportStarted -> copy(
                isImportingAttachments = true
            )

            is Msg.AttachmentsAdded -> copy(
                isImportingAttachments = false,
                promptImages = promptImages + msg.attachments
            )

            is Msg.AttachmentsRestored -> copy(
                promptImages = promptImages + msg.attachments
            )

            is Msg.AttachmentRemoved -> copy(
                promptImages = promptImages - msg.attachment
            )

            Msg.AttachmentsCleared -> copy(
                promptImages = emptyList()
            )
        }
    }

    companion object {

        private val SEARCH_QUERY_DEBOUNCE = 300.milliseconds
        private val MODEL_SELECTED_INDICATOR_DURATION = 1500.milliseconds
    }
}
