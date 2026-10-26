package com.nullo.voidapp.feature.chat.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.utils.decompose.componentScope
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStoreFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DefaultChatComponent(
    private val onNavigateToModelCatalog: () -> Unit,
    private val onNavigateToAppSettings: () -> Unit,
    private val chatStoreFactory: ChatStoreFactory,
    private val componentContext: ComponentContext,
) : ChatComponent,
    ComponentContext by componentContext {

    private val scope = componentScope()
    private val store = instanceKeeper.getStore { chatStoreFactory.create() }

    init {
        observeLabels()
    }

    override val model: Value<ChatStore.State> = store.asValue()

    override fun onOpenModelSelectorClicked() {
        store.accept(Intent.OpenModelSelector)
    }

    override fun onCloseModelSelectorRequested() {
        store.accept(Intent.CloseModelSelector)
    }

    override fun onChangeModelSelectorTab(tab: ChatStore.State.SelectorTab) {
        store.accept(Intent.ChangeModelSelectorTab(tab))
    }

    override fun onModelSearchQueryChanged(query: String) {
        store.accept(Intent.ChangeModelSearchQuery(query))
    }

    override fun onModelSelected(modelRef: ActiveModelRef) {
        store.accept(Intent.SelectModel(modelRef))
    }

    override fun onModelUnpinned(modelId: String, modelType: ModelType) {
        store.accept(Intent.UnpinModel(modelId, modelType))
    }

    override fun onTuningPanelOpened() {
        store.accept(Intent.OpenTuningPanel)
    }

    override fun onTuningPanelClosed() {
        store.accept(Intent.CloseTuningPanel)
    }

    override fun onAttachmentSelectorOpened() {
        store.accept(Intent.OpenAttachmentsSelector)
    }

    override fun onThinkingToggled() {
        store.accept(Intent.ToggleThinking)
    }

    override fun onWebSearchToggled() {
        store.accept(Intent.ToggleWebSearch)
    }

    override fun onPromptChanged(prompt: String) {
        store.accept(Intent.ChangePrompt(prompt))
    }

    override fun onPromptSent() {
        store.accept(Intent.SendPrompt)
    }

    override fun onLastResponseRegenerateRequested() {
        store.accept(Intent.RegenerateLastResponse)
    }

    override fun onGenerationStopped() {
        store.accept(Intent.StopGeneration)
    }

    override fun onChatErrorDismissed(messageId: Long) {
        store.accept(Intent.DismissChatError(messageId))
    }

    override fun onErrorDialogDismissed() {
        store.accept(Intent.DismissErrorDialog)
    }

    override fun onNewConversationStarted() {
        store.accept(Intent.StartNewConversation)
    }

    override fun onConversationOpened(conversationId: Long) {
        store.accept(Intent.OpenConversation(conversationId))
    }

    override fun onSearchConversationQueryChanged(query: String) {
        store.accept(Intent.ChangeConversationSearchQuery(query))
    }

    override fun onRenameConversationDialogOpened(conversationId: Long) {
        store.accept(Intent.OpenRenameConversationDialog(conversationId))
    }

    override fun onRenameConversationDialogClosed() {
        store.accept(Intent.CloseRenameConversationDialog)
    }

    override fun onConversationNameInputChange(input: String) {
        store.accept(Intent.ChangeConversationNameInput(input))
    }

    override fun onConversationRenameConfirmed() {
        store.accept(Intent.ConfirmConversationRename)
    }

    override fun onConversationDeleted(conversationId: Long) {
        store.accept(Intent.DeleteConversation(conversationId))
    }

    override fun onFullModelCatalogOpened() {
        store.accept(Intent.OpenFullModelCatalog)
    }

    override fun onAppSettingsOpened() {
        store.accept(Intent.OpenAppSettings)
    }

    private fun observeLabels() {
        store.labels
            .onEach { label ->
                when (label) {
                    ChatStore.Label.NavigatedToAppSettings -> onNavigateToAppSettings()
                    ChatStore.Label.NavigatedToModelCatalog -> onNavigateToModelCatalog()
                }
            }
            .launchIn(scope)
    }
}
