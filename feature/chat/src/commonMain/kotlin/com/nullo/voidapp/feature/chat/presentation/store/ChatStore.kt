package com.nullo.voidapp.feature.chat.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.resources.UiText

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> {

    @Immutable
    data class State(
        val prompt: String = "",
        val conversationSearchQuery: String = "",
        val modelSearchQuery: String = "",

        val activeConversation: Conversation? = null,
        val messages: List<Message> = emptyList(),

        val activeModelName: String? = null,
        val modelType: ModelType? = null,
        val isModelSelectionConfirmationShown: Boolean = false,

        val thinkingEnabled: Boolean = false,
        val webSearchEnabled: Boolean = false,

        val isModelSelectorOpen: Boolean = false,
        val modelSelectorTab: SelectorTab = SelectorTab.ChatModels,
        val pinnedChatModels: List<ChatModel> = emptyList(),
        val pinnedImageModels: List<ImageModel> = emptyList(),
        val conversations: List<Conversation> = emptyList(),

        val isSideMenuOpen: Boolean = false,
        val isTuningPanelOpen: Boolean = false,
        val errorDialogText: UiText? = null,

        val renamingConversationId: Long? = null,
        val renamingConversationInput: String = "",
    ) {
        val isGenerating: Boolean
            get() = (messages.lastOrNull() as? Message.Completion)?.isStreaming ?: false

        @Immutable
        sealed interface SelectorTab {

            data object ChatModels : SelectorTab
            data object ImageModels : SelectorTab
        }
    }

    sealed interface Intent {
        data object OpenModelSelector : Intent
        data object CloseModelSelector : Intent
        data class ChangeModelSelectorTab(val tab: State.SelectorTab) : Intent
        data class ChangeModelSearchQuery(val query: String) : Intent
        data class SelectModel(val activeModelRef: ActiveModelRef) : Intent
        data class UnpinModel(
            val modelId: String,
            val modelType: ModelType,
        ) : Intent

        data object OpenTuningPanel : Intent
        data object CloseTuningPanel : Intent

        data object OpenAttachmentsSelector : Intent
        data object ToggleThinking : Intent
        data object ToggleWebSearch : Intent

        data class ChangePrompt(val prompt: String) : Intent
        data object SendPrompt : Intent
        data object RegenerateLastResponse : Intent
        data object StopGeneration : Intent

        data class DismissChatError(val messageId: Long) : Intent
        data object DismissErrorDialog : Intent

        data object StartNewConversation : Intent
        data class OpenConversation(val conversationId: Long) : Intent
        data class ChangeConversationSearchQuery(val query: String) : Intent
        data class DeleteConversation(val conversationId: Long) : Intent

        data class OpenRenameConversationDialog(val conversationId: Long) : Intent
        data object CloseRenameConversationDialog : Intent
        data class ChangeConversationNameInput(val input: String) : Intent
        data object ConfirmConversationRename : Intent

        data object OpenFullModelCatalog : Intent
        data object OpenAppSettings : Intent
    }

    sealed interface Label {

        data object NavigatedToModelCatalog : Label
        data object NavigatedToAppSettings : Label
    }
}
