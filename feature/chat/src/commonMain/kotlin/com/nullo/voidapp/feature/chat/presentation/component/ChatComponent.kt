package com.nullo.voidapp.feature.chat.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore

interface ChatComponent {

    val model: Value<ChatStore.State>

    fun onOpenModelSelectorClicked()

    fun onCloseModelSelectorRequested()

    fun onChangeModelSelectorTab(tab: ChatStore.State.SelectorTab)

    fun onModelSearchQueryChanged(query: String)

    fun onModelSelected(modelRef: ActiveModelRef)

    fun onModelUnpinned(modelId: String, modelType: ModelType)

    fun onTuningPanelOpened()

    fun onTuningPanelClosed()

    fun onAttachmentSelectorOpened()

    fun onThinkingToggled()

    fun onWebSearchToggled()

    fun onPromptChanged(prompt: String)

    fun onPromptSent()

    fun onLastResponseRegenerateRequested()

    fun onGenerationStopped()

    fun onChatErrorDismissed(messageId: Long)

    fun onErrorDialogDismissed()

    fun onNewConversationStarted()

    fun onConversationOpened(conversationId: Long)

    fun onSearchConversationQueryChanged(query: String)

    fun onRenameConversationDialogOpened(conversationId: Long)

    fun onRenameConversationDialogClosed()

    fun onConversationNameInputChange(input: String)

    fun onConversationRenameConfirmed()

    fun onConversationDeleted(conversationId: Long)

    fun onFullModelCatalogOpened()

    fun onAppSettingsOpened()

    fun interface Factory {

        fun create(
            onNavigateToModelCatalog: () -> Unit,
            onNavigateToAppSettings: () -> Unit,
            componentContext: ComponentContext,
        ): ChatComponent
    }
}
