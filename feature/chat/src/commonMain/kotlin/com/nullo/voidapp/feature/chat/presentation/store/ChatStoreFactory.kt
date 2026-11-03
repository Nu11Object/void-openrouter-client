package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.domain.DeleteAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.DismissErrorMessageUseCase
import com.nullo.voidapp.feature.chat.domain.ImportAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveMessagesUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.RegenerateLastResponseUseCase
import com.nullo.voidapp.feature.chat.domain.SendPromptUseCase
import com.nullo.voidapp.feature.chat.domain.SetThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.SetWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.StartNewConversationUseCase
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.State
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.error_empty_prompt_on_image
import voidapp.feature.chat.generated.resources.error_empty_request
import voidapp.feature.chat.generated.resources.error_model_no_image_input
import voidapp.feature.chat.generated.resources.error_model_not_selected
import voidapp.feature.chat.generated.resources.error_no_active_conversation
import voidapp.feature.chat.generated.resources.error_some_attachments_import_failed
import voidapp.feature.chat.generated.resources.template_error_max_attachments_reached

internal class ChatStoreFactory(
    private val storeFactory: StoreFactory,
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
    private val sendPromptUseCase: SendPromptUseCase,
    private val importAttachmentUseCase: ImportAttachmentUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase,
) : StoreFactory by storeFactory {

    fun create(activeConversation: ActiveConversationHolder): ChatStore = object :
        ChatStore,
        Store<Intent, State, Nothing> by storeFactory.create(
            name = "ChatStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(activeConversation),
            executorFactory = { ExecutorImpl(activeConversation) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {

        data class ActiveModelChanged(
            val modelType: ModelType,
            val modelName: String,
            val supportsImageInput: Boolean,
        ) : Msg

        data class MessagesUpdated(val messages: List<Message>) : Msg

        data object TuningPanelOpened : Msg
        data object TuningPanelClosed : Msg

        data class ThinkingStateChanged(val enabled: Boolean) : Msg
        data class WebSearchStateChanged(val enabled: Boolean) : Msg

        data class PromptChanged(val prompt: String) : Msg

        data class ErrorOccurred(val text: UiText) : Msg
        data object ErrorDialogDismissed : Msg

        data object NewConversationStarted : Msg

        data object AttachmentsImportStarted : Msg
        data class AttachmentsAdded(val attachments: List<ImageAttachment>) : Msg
        data class AttachmentsRestored(val attachments: List<ImageAttachment>) : Msg
        data object AttachmentsCleared : Msg
        data class AttachmentRemoved(val attachment: ImageAttachment) : Msg
    }

    private sealed interface Action {

        data class UpdateActiveModel(val activeModelRef: ActiveModelRef?) : Action
        data class UpdateActiveConversation(val conversation: Conversation?) : Action
        data class ConversationIdChanged(val conversationId: Long?) : Action

        data class UpdateMessages(val messages: List<Message>) : Action

        data class UpdateThinkingEnabled(val enabled: Boolean) : Action
        data class UpdateWebSearchEnabled(val enabled: Boolean) : Action
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private inner class BootstrapperImpl(private val activeConversation: ActiveConversationHolder) :
        CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeActiveModel()
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

        private fun observeActiveConversation() {
            activeConversation.id
                .onEach { dispatch(Action.ConversationIdChanged(it)) }
                .flatMapLatest { id ->
                    if (id == null) flowOf(null) else observeConversationByIdUseCase(id)
                }
                .onEach { dispatch(Action.UpdateActiveConversation(it)) }
                .launchIn(scope)
        }

        private fun observeMessages() {
            activeConversation.id
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

    private inner class ExecutorImpl(private val activeConversation: ActiveConversationHolder) :
        CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {
        private var activeModelRef: ActiveModelRef? = null
        private var activeModelNameJob: Job? = null

        private var completionGenerationJob: Job? = null

        // The last conversation id seen or set by this executor. Lets us tell a change made
        // from outside (side menu) apart from one made by this executor itself.
        private var observedConversationId: Long? = null

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

                is Action.ConversationIdChanged -> {
                    if (action.conversationId != observedConversationId) {
                        completionGenerationJob?.cancel()
                        observedConversationId = action.conversationId
                    }
                }

                is Action.UpdateMessages -> dispatch(
                    Msg.MessagesUpdated(action.messages)
                )

                is Action.UpdateActiveConversation -> {
                    if (action.conversation == null) {
                        dispatch(Msg.NewConversationStarted)
                        setConversationId(null)
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
                Intent.CloseTuningPanel -> dispatch(Msg.TuningPanelClosed)

                Intent.DismissErrorDialog -> dispatch(Msg.ErrorDialogDismissed)

                is Intent.DismissChatError -> scope.launch {
                    dismissErrorMessageUseCase(intent.messageId)
                }

                Intent.RegenerateLastResponse -> {
                    if (completionGenerationJob?.isActive == true) return

                    val conversationId = activeConversation.id.value ?: run {
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
                            val conversationId = activeConversation.id.value
                                ?: startNewConversationUseCase(prompt).also { setConversationId(it) }

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

                is Intent.ChangePrompt -> dispatch(Msg.PromptChanged(intent.prompt))

                Intent.OpenTuningPanel -> {
                    dispatch(Msg.ErrorOccurred(UiText.Static("Not implemented yet")))
                }

                Intent.StopGeneration -> {
                    completionGenerationJob?.cancel()
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

        private fun setConversationId(id: Long?) {
            observedConversationId = id
            activeConversation.set(id)
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

            Msg.ErrorDialogDismissed -> copy(
                errorDialogText = null
            )

            is Msg.ErrorOccurred -> copy(
                errorDialogText = msg.text
            )

            is Msg.PromptChanged -> copy(
                prompt = msg.prompt
            )

            Msg.NewConversationStarted -> copy(
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
}
