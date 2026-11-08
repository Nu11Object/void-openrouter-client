package com.nullo.voidapp.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.EdgeFadeSpec
import com.nullo.voidapp.core.utils.compose.FadeDirection
import com.nullo.voidapp.core.utils.compose.SmoothStepEasing
import com.nullo.voidapp.core.utils.compose.edgeFades
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.presentation.component.ChatComponent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStore
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore
import com.nullo.voidapp.feature.chat.presentation.ui.composer.ComposerActions
import com.nullo.voidapp.feature.chat.presentation.ui.composer.PromptComposer
import com.nullo.voidapp.feature.chat.presentation.ui.messages.MessageActions
import com.nullo.voidapp.feature.chat.presentation.ui.messages.MessageList
import com.nullo.voidapp.feature.chat.presentation.ui.modelselector.ModelSelectorActions
import com.nullo.voidapp.feature.chat.presentation.ui.modelselector.ModelSelectorSheet
import com.nullo.voidapp.feature.chat.presentation.ui.sidemenu.RenameConversationDialog
import com.nullo.voidapp.feature.chat.presentation.ui.sidemenu.SideMenu
import com.nullo.voidapp.feature.chat.presentation.ui.sidemenu.SideMenuActions
import com.nullo.voidapp.feature.chat.presentation.ui.tune.TuneActions
import com.nullo.voidapp.feature.chat.presentation.ui.tune.TuneDrawerHost
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.select_model

@Composable
fun ChatScreen(
    component: ChatComponent,
    modifier: Modifier = Modifier,
) {
    val chatState by component.model.subscribeAsState()
    val sideMenuState by component.sideMenuModel.subscribeAsState()
    val modelSelectorState by component.modelSelectorModel.subscribeAsState()
    val tuneState by component.tuneModel.subscribeAsState()

    val sideMenuActions = remember(component) {
        SideMenuActions(
            onStartNewConversation = component::onNewConversationStarted,
            onConversationQueryChange = component::onSearchConversationQueryChanged,
            onSelectConversation = component::onConversationOpened,
            onNavigateToModelCatalog = component::onFullModelCatalogOpened,
            onNavigateToAppSettings = component::onAppSettingsOpened,
            onEditConversationTitleClick = component::onRenameConversationDialogOpened,
            onDeleteConversationClick = component::onConversationDeleted,
            onDismissRenameDialog = component::onRenameConversationDialogClosed,
            onConversationNameInputChange = component::onConversationNameInputChange,
            onConfirmConversationRename = component::onConversationRenameConfirmed,
            onDismissErrorDialog = component::onSideMenuErrorDialogDismissed
        )
    }
    val modelSelectorActions = remember(component) {
        ModelSelectorActions(
            onDismiss = component::onCloseModelSelectorRequested,
            onTabChange = component::onChangeModelSelectorTab,
            onSearchQueryChange = component::onModelSearchQueryChanged,
            onUseModel = component::onModelSelected,
            onUnpinModel = component::onModelUnpinned,
            onNavigateToModelCatalog = component::onFullModelCatalogOpened
        )
    }
    val composerActions = remember(component) {
        ComposerActions(
            onPromptChange = component::onPromptChanged,
            onSendPrompt = component::onPromptSent,
            onStopGeneration = component::onGenerationStopped,
            onToggleThinking = component::onThinkingToggled,
            onToggleWebSearch = component::onWebSearchToggled,
            onAttachImages = component::onImagesAttached,
            onRemoveAttachment = component::onImageAttachmentDeleted
        )
    }
    val messageActions = remember(component) {
        MessageActions(
            onRegenerateLastResponse = component::onLastResponseRegenerateRequested,
            onSaveImage = component::onImageSaved,
            onDismissChatError = component::onChatErrorDismissed
        )
    }
    val tuneActions = remember(component) {
        TuneActions(
            onClose = component::onTuningPanelClosed,
            onHintsVisibilityChange = component::onTuningHintsVisibilityChanged,
            onReset = component::onTuningResetClicked,
            onChatValueChange = component::onChatTuningValueChanged,
            onChatSliderChange = component::onChatTuningSliderChanged,
            onMaxTokensSetToLimit = component::onMaxTokensSetToLimit,
            onChatSeedRandomize = component::onChatSeedRandomized,
            onImageOptionSelect = component::onImageOptionSelected,
            onImageValueChange = component::onImageValueChanged,
            onImageSliderChange = component::onImageSliderChanged,
            onImageSeedRandomize = component::onImageSeedRandomized
        )
    }

    ChatScreenContent(
        modifier = modifier,
        chatState = chatState,
        sideMenuState = sideMenuState,
        modelSelectorState = modelSelectorState,
        tuneState = tuneState,
        sideMenuActions = sideMenuActions,
        modelSelectorActions = modelSelectorActions,
        composerActions = composerActions,
        messageActions = messageActions,
        tuneActions = tuneActions,
        onOpenModelSelectorClick = component::onOpenModelSelectorClicked,
        onOpenTuningPanelClick = component::onTuningPanelOpened,
        onDismissErrorDialog = component::onErrorDialogDismissed
    )
}

@Composable
private fun ChatScreenContent(
    chatState: ChatStore.State,
    sideMenuState: SideMenuStore.State,
    modelSelectorState: ModelSelectorStore.State,
    tuneState: TuneStore.State,
    sideMenuActions: SideMenuActions,
    modelSelectorActions: ModelSelectorActions,
    composerActions: ComposerActions,
    messageActions: MessageActions,
    tuneActions: TuneActions,
    onOpenModelSelectorClick: () -> Unit,
    onOpenTuningPanelClick: () -> Unit,
    onDismissErrorDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ime = WindowInsets.ime

    val scope = rememberCoroutineScope()
    val deviceConfiguration = rememberDeviceConfiguration()
    val sideMenuRailState = rememberWideNavigationRailState()

    val imeVisible by remember { derivedStateOf { ime.getBottom(density) > 0 } }
    val shouldHideAppBar = deviceConfiguration == DeviceConfiguration.MOBILE_LANDSCAPE && imeVisible
    val sideMenuIsAlwaysExpanded = (deviceConfiguration == DeviceConfiguration.TABLET_LANDSCAPE) ||
        (deviceConfiguration == DeviceConfiguration.DESKTOP)

    TuneDrawerHost(
        state = tuneState,
        actions = tuneActions,
        modifier = modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            SideMenu(
                railState = sideMenuRailState,
                alwaysExpanded = sideMenuIsAlwaysExpanded,
                conversations = sideMenuState.conversations,
                activeConversationId = sideMenuState.activeConversationId,
                conversationQuery = sideMenuState.conversationSearchQuery,
                actions = sideMenuActions,
                modifier = Modifier.fillMaxHeight()
            )

            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    AnimatedVisibility(
                        visible = !shouldHideAppBar,
                        label = "TopAppBarAnimation",
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ChatTopAppBar(
                            isSideMenuAlwaysExpanded = sideMenuIsAlwaysExpanded,
                            activeModelName = chatState.activeModelName ?: stringResource(Res.string.select_model),
                            onNavigationIconClick = { scope.launch { sideMenuRailState.expand() } },
                            onOpenModelSelectorClick = onOpenModelSelectorClick,
                            onOpenTuningPanelClick = onOpenTuningPanelClick
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                        .consumeWindowInsets(innerPadding)
                        .imePadding()
                        .chatPlaceholderGlow(visible = chatState.messages.isEmpty())
                ) {
                    MessageList(
                        messages = chatState.messages,
                        messagesTopPadding = innerPadding.calculateTopPadding(),
                        actions = messageActions,
                        modifier = Modifier
                            .weight(1f)
                            .edgeFades(
                                EdgeFadeSpec(
                                    direction = FadeDirection.Top,
                                    fadeSize = innerPadding.calculateTopPadding(),
                                    holdFraction = 0.5f,
                                    easing = SmoothStepEasing
                                ),
                                EdgeFadeSpec(
                                    direction = FadeDirection.Bottom,
                                    sizeFraction = 0.05f
                                )
                            )
                    )

                    PromptComposer(
                        prompt = chatState.prompt,
                        promptImages = chatState.promptImages,
                        isGenerating = chatState.isGenerating,
                        isImportingAttachments = chatState.isImportingAttachments,
                        canAttachImages = chatState.canAttachImages,
                        canSendPrompt = chatState.canSendPrompt,
                        thinkingEnabled = chatState.thinkingEnabled,
                        webSearchEnabled = chatState.webSearchEnabled,
                        actions = composerActions,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    )
                }

                EmptyChatPlaceholder(visible = chatState.messages.isEmpty())
            }
        }
    }

    if (modelSelectorState.isModelSelectorOpen) {
        ModelSelectorSheet(
            selectedTab = modelSelectorState.modelSelectorTab,
            modelSearchQuery = modelSelectorState.modelSearchQuery,
            pinnedChatModels = modelSelectorState.pinnedChatModels,
            pinnedImageModels = modelSelectorState.pinnedImageModels,
            actions = modelSelectorActions
        )
    }

    if (sideMenuState.renamingConversationId != null) {
        RenameConversationDialog(
            conversationTitle = sideMenuState.renamingConversationInput,
            onDismissRenameConversationDialog = sideMenuActions.onDismissRenameDialog,
            onConversationTitleChange = sideMenuActions.onConversationNameInputChange,
            onConfirmConversationRename = sideMenuActions.onConfirmConversationRename
        )
    }

    chatState.errorDialogText?.let { errorUiText ->
        ChatErrorDialog(
            text = errorUiText,
            onDismiss = onDismissErrorDialog
        )
    }

    sideMenuState.errorDialogText?.let { errorUiText ->
        ChatErrorDialog(
            text = errorUiText,
            onDismiss = sideMenuActions.onDismissErrorDialog
        )
    }
}

@Composable
private fun ChatErrorDialog(
    text: UiText,
    onDismiss: () -> Unit,
) {
    VoidAlertDialog(
        title = stringResource(CoreRes.string.oops),
        message = text.asString(),
        onDismissRequest = onDismiss,
        confirmButton = VoidDialogButton(
            text = stringResource(CoreRes.string.ok),
            onClick = onDismiss
        )
    )
}

@Composable
@PreviewLightDark
private fun ChatScreenPreview() {
    VoidTheme {
        ChatScreenContent(
            chatState = ChatStore.State(
                prompt = "",
                thinkingEnabled = true,
                webSearchEnabled = false,
                messages = listOf(
                    Message.Prompt(
                        id = 0,
                        query = "Prompt message",
                        createdAt = 11111111111
                    ),
                    Message.Completion(
                        id = 1,
                        text = "Fully generated response",
                        createdAt = 22222222222,
                        modelId = "company/model-id"
                    ),
                    Message.Prompt(
                        id = 2,
                        query = "Another prompt",
                        createdAt = 33333333333
                    ),
                    Message.Completion(
                        id = 3,
                        reasoning = "Reasoning text from AI is here",
                        text = "This is a generating response from AI. One, two, three, fo",
                        isStreaming = true
                    )
                )
            ),
            sideMenuState = SideMenuStore.State(
                conversations = listOf(
                    Conversation(
                        id = 1,
                        title = "Title of a conversation",
                        createdAt = 111111111L,
                        updatedAt = 111111111L
                    ),
                    Conversation(
                        id = 2,
                        title = "Title of selected one",
                        createdAt = 222222222L,
                        updatedAt = 222222222L
                    ),
                    Conversation(
                        id = 3,
                        title = "Long title with ellipsis overflow example",
                        createdAt = 333333333L,
                        updatedAt = 333333333L
                    )
                ),
                activeConversationId = 2
            ),
            modelSelectorState = ModelSelectorStore.State(),
            tuneState = TuneStore.State(),
            sideMenuActions = SideMenuActions(),
            modelSelectorActions = ModelSelectorActions(),
            composerActions = ComposerActions(),
            messageActions = MessageActions(),
            tuneActions = TuneActions(),
            onOpenModelSelectorClick = {},
            onOpenTuningPanelClick = {},
            onDismissErrorDialog = {}
        )
    }
}
