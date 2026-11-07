package com.nullo.voidapp.feature.chat.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import com.nullo.voidapp.feature.chat.presentation.store.ActiveConversationHolder
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore
import com.nullo.voidapp.feature.chat.presentation.store.ChatStoreFactory
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStoreFactory
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStore
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStoreFactory
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore
import com.nullo.voidapp.feature.chat.presentation.store.TuneStoreFactory
import io.github.vinceglb.filekit.PlatformFile

internal class DefaultChatComponent(
    private val onNavigateToModelCatalog: () -> Unit,
    private val onNavigateToAppSettings: () -> Unit,
    chatStoreFactory: ChatStoreFactory,
    sideMenuStoreFactory: SideMenuStoreFactory,
    modelSelectorStoreFactory: ModelSelectorStoreFactory,
    tuneStoreFactory: TuneStoreFactory,
    private val componentContext: ComponentContext,
) : ChatComponent,
    ComponentContext by componentContext {

    // Retained with the stores so a recreated component reuses the same holder.
    private val activeConversation = instanceKeeper
        .getOrCreate { RetainedActiveConversation() }
        .holder

    private val chatStore = instanceKeeper.getStore {
        chatStoreFactory.create(activeConversation)
    }
    private val sideMenuStore = instanceKeeper.getStore {
        sideMenuStoreFactory.create(activeConversation)
    }
    private val modelSelectorStore = instanceKeeper.getStore {
        modelSelectorStoreFactory.create()
    }
    private val tuneStore = instanceKeeper.getStore {
        tuneStoreFactory.create()
    }

    private val tuningPanelBackCallback = BackCallback(
        isEnabled = tuneStore.state.isPanelOpen,
        onBack = { onTuningPanelClosed() }
    )

    init {
        backHandler.register(tuningPanelBackCallback)
    }

    override val model: Value<ChatStore.State> = chatStore.asValue()
    override val sideMenuModel: Value<SideMenuStore.State> = sideMenuStore.asValue()
    override val modelSelectorModel: Value<ModelSelectorStore.State> = modelSelectorStore.asValue()
    override val tuneModel: Value<TuneStore.State> = tuneStore.asValue()

    override fun onOpenModelSelectorClicked() {
        modelSelectorStore.accept(ModelSelectorStore.Intent.OpenModelSelector)
    }

    override fun onCloseModelSelectorRequested() {
        modelSelectorStore.accept(ModelSelectorStore.Intent.CloseModelSelector)
    }

    override fun onChangeModelSelectorTab(tab: ModelSelectorStore.State.SelectorTab) {
        modelSelectorStore.accept(ModelSelectorStore.Intent.ChangeModelSelectorTab(tab))
    }

    override fun onModelSearchQueryChanged(query: String) {
        modelSelectorStore.accept(ModelSelectorStore.Intent.ChangeModelSearchQuery(query))
    }

    override fun onModelSelected(modelRef: ActiveModelRef) {
        modelSelectorStore.accept(ModelSelectorStore.Intent.SelectModel(modelRef))
    }

    override fun onModelUnpinned(modelId: String, modelType: ModelType) {
        modelSelectorStore.accept(ModelSelectorStore.Intent.UnpinModel(modelId, modelType))
    }

    override fun onTuningPanelOpened() {
        tuningPanelBackCallback.isEnabled = true
        tuneStore.accept(TuneStore.Intent.OpenPanel)
    }

    override fun onTuningPanelClosed() {
        tuningPanelBackCallback.isEnabled = false
        tuneStore.accept(TuneStore.Intent.ClosePanel)
    }

    override fun onTuningHintsVisibilityChanged(visible: Boolean) {
        tuneStore.accept(TuneStore.Intent.ChangeHintsVisibility(visible))
    }

    override fun onTuningResetClicked() {
        tuneStore.accept(TuneStore.Intent.ResetAll)
    }

    override fun onChatTuningValueChanged(parameter: TuningParameter, raw: String) {
        tuneStore.accept(TuneStore.Intent.ChangeChatValue(parameter, raw))
    }

    override fun onChatTuningSliderChanged(parameter: TuningParameter, value: Double) {
        tuneStore.accept(TuneStore.Intent.ChangeChatSlider(parameter, value))
    }

    override fun onMaxTokensSetToLimit() {
        tuneStore.accept(TuneStore.Intent.SetMaxTokensToLimit)
    }

    override fun onChatSeedRandomized() {
        tuneStore.accept(TuneStore.Intent.RandomizeChatSeed)
    }

    override fun onImageOptionSelected(parameter: ImageParameter, option: String) {
        tuneStore.accept(TuneStore.Intent.SelectImageOption(parameter, option))
    }

    override fun onImageValueChanged(parameter: ImageParameter, raw: String) {
        tuneStore.accept(TuneStore.Intent.ChangeImageValue(parameter, raw))
    }

    override fun onImageSliderChanged(parameter: ImageParameter, value: Double) {
        tuneStore.accept(TuneStore.Intent.ChangeImageSlider(parameter, value))
    }

    override fun onImageSeedRandomized() {
        tuneStore.accept(TuneStore.Intent.RandomizeImageSeed)
    }

    override fun onImagesAttached(files: List<PlatformFile>) {
        chatStore.accept(ChatStore.Intent.AttachImages(files))
    }

    override fun onImageAttachmentDeleted(image: ImageAttachment) {
        chatStore.accept(ChatStore.Intent.RemoveImageAttachment(image))
    }

    override fun onImageSaved(image: ImageAttachment, destination: PlatformFile) {
        chatStore.accept(ChatStore.Intent.SaveImage(image, destination))
    }

    override fun onThinkingToggled() {
        chatStore.accept(ChatStore.Intent.ToggleThinking)
    }

    override fun onWebSearchToggled() {
        chatStore.accept(ChatStore.Intent.ToggleWebSearch)
    }

    override fun onPromptChanged(prompt: String) {
        chatStore.accept(ChatStore.Intent.ChangePrompt(prompt))
    }

    override fun onPromptSent() {
        chatStore.accept(ChatStore.Intent.SendPrompt)
    }

    override fun onLastResponseRegenerateRequested() {
        chatStore.accept(ChatStore.Intent.RegenerateLastResponse)
    }

    override fun onGenerationStopped() {
        chatStore.accept(ChatStore.Intent.StopGeneration)
    }

    override fun onChatErrorDismissed(messageId: Long) {
        chatStore.accept(ChatStore.Intent.DismissChatError(messageId))
    }

    override fun onErrorDialogDismissed() {
        chatStore.accept(ChatStore.Intent.DismissErrorDialog)
    }

    override fun onNewConversationStarted() {
        sideMenuStore.accept(SideMenuStore.Intent.StartNewConversation)
    }

    override fun onConversationOpened(conversationId: Long) {
        sideMenuStore.accept(SideMenuStore.Intent.OpenConversation(conversationId))
    }

    override fun onSearchConversationQueryChanged(query: String) {
        sideMenuStore.accept(SideMenuStore.Intent.ChangeConversationSearchQuery(query))
    }

    override fun onRenameConversationDialogOpened(conversationId: Long) {
        sideMenuStore.accept(SideMenuStore.Intent.OpenRenameConversationDialog(conversationId))
    }

    override fun onRenameConversationDialogClosed() {
        sideMenuStore.accept(SideMenuStore.Intent.CloseRenameConversationDialog)
    }

    override fun onConversationNameInputChange(input: String) {
        sideMenuStore.accept(SideMenuStore.Intent.ChangeConversationNameInput(input))
    }

    override fun onConversationRenameConfirmed() {
        sideMenuStore.accept(SideMenuStore.Intent.ConfirmConversationRename)
    }

    override fun onConversationDeleted(conversationId: Long) {
        sideMenuStore.accept(SideMenuStore.Intent.DeleteConversation(conversationId))
    }

    override fun onSideMenuErrorDialogDismissed() {
        sideMenuStore.accept(SideMenuStore.Intent.DismissErrorDialog)
    }

    override fun onFullModelCatalogOpened() {
        onNavigateToModelCatalog()
    }

    override fun onAppSettingsOpened() {
        onNavigateToAppSettings()
    }

    private class RetainedActiveConversation : InstanceKeeper.Instance {
        val holder = ActiveConversationHolder()

        override fun onDestroy() = Unit
    }
}
