package com.nullo.voidapp.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.component.VoidSearchBar
import com.nullo.voidapp.core.designsystem.component.VoidTextField
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ChevronRight
import com.nullo.voidapp.core.designsystem.icon.automirrored.LeftPanelClose
import com.nullo.voidapp.core.designsystem.icon.automirrored.MenuOpen
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.ArrowDropDown
import com.nullo.voidapp.core.designsystem.icon.default.ArrowUpward
import com.nullo.voidapp.core.designsystem.icon.default.Attach
import com.nullo.voidapp.core.designsystem.icon.default.Awesome
import com.nullo.voidapp.core.designsystem.icon.default.Chat
import com.nullo.voidapp.core.designsystem.icon.default.Close
import com.nullo.voidapp.core.designsystem.icon.default.ContentCopy
import com.nullo.voidapp.core.designsystem.icon.default.DeleteForever
import com.nullo.voidapp.core.designsystem.icon.default.Explore
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.core.designsystem.icon.default.KeepOff
import com.nullo.voidapp.core.designsystem.icon.default.Lightbulb
import com.nullo.voidapp.core.designsystem.icon.default.Logo
import com.nullo.voidapp.core.designsystem.icon.default.MoreVert
import com.nullo.voidapp.core.designsystem.icon.default.SearchOff
import com.nullo.voidapp.core.designsystem.icon.default.Settings
import com.nullo.voidapp.core.designsystem.icon.default.Stop
import com.nullo.voidapp.core.designsystem.icon.default.Stylus
import com.nullo.voidapp.core.designsystem.icon.default.Tune
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.designsystem.theme.chatMarkdownTypography
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.EdgeFadeSpec
import com.nullo.voidapp.core.utils.compose.FadeDirection
import com.nullo.voidapp.core.utils.compose.SmoothStepEasing
import com.nullo.voidapp.core.utils.compose.edgeFades
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.chat.presentation.component.ChatComponent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.State.SelectorTab
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.cancel
import voidapp.feature.chat.generated.resources.chat_models
import voidapp.feature.chat.generated.resources.delete
import voidapp.feature.chat.generated.resources.desc_attach_files
import voidapp.feature.chat.generated.resources.desc_collapse_side_menu
import voidapp.feature.chat.generated.resources.desc_copy_last_response
import voidapp.feature.chat.generated.resources.desc_open_side_menu
import voidapp.feature.chat.generated.resources.desc_regenerate_last_response
import voidapp.feature.chat.generated.resources.desc_send_message
import voidapp.feature.chat.generated.resources.desc_stop_generation
import voidapp.feature.chat.generated.resources.dismiss
import voidapp.feature.chat.generated.resources.empty_chat_placeholder
import voidapp.feature.chat.generated.resources.error_details
import voidapp.feature.chat.generated.resources.error_title_credits
import voidapp.feature.chat.generated.resources.error_title_network
import voidapp.feature.chat.generated.resources.error_title_rate_limit
import voidapp.feature.chat.generated.resources.error_title_server
import voidapp.feature.chat.generated.resources.error_title_unauthorized
import voidapp.feature.chat.generated.resources.error_title_unexpected
import voidapp.feature.chat.generated.resources.hide_reasoning
import voidapp.feature.chat.generated.resources.image_models
import voidapp.feature.chat.generated.resources.loading_generation
import voidapp.feature.chat.generated.resources.model_catalog
import voidapp.feature.chat.generated.resources.new_chat
import voidapp.feature.chat.generated.resources.no_models_found
import voidapp.feature.chat.generated.resources.no_pinned_models
import voidapp.feature.chat.generated.resources.no_text_was_generated
import voidapp.feature.chat.generated.resources.open_model_catalog
import voidapp.feature.chat.generated.resources.option_search
import voidapp.feature.chat.generated.resources.option_think
import voidapp.feature.chat.generated.resources.placeholder_empty_prompt
import voidapp.feature.chat.generated.resources.placeholder_new_conversation_title
import voidapp.feature.chat.generated.resources.placeholder_no_conversations
import voidapp.feature.chat.generated.resources.placeholder_no_conversations_found
import voidapp.feature.chat.generated.resources.placeholder_search_chats
import voidapp.feature.chat.generated.resources.rename
import voidapp.feature.chat.generated.resources.rename_conversation_desc
import voidapp.feature.chat.generated.resources.rename_conversation_title
import voidapp.feature.chat.generated.resources.retry
import voidapp.feature.chat.generated.resources.search_models
import voidapp.feature.chat.generated.resources.select_model
import voidapp.feature.chat.generated.resources.settings
import voidapp.feature.chat.generated.resources.show_reasoning
import voidapp.feature.chat.generated.resources.template_delete
import voidapp.feature.chat.generated.resources.template_desc_active_model
import voidapp.feature.chat.generated.resources.template_desc_more_conv_actions
import voidapp.feature.chat.generated.resources.template_rename
import voidapp.feature.chat.generated.resources.template_unpin_model
import voidapp.feature.chat.generated.resources.textbutton_retry_interupted_response
import voidapp.feature.chat.generated.resources.tune_model
import voidapp.feature.chat.generated.resources.untitled_conversation
import voidapp.feature.chat.generated.resources.void

private const val PROMPT_MAX_LINES = 6
private val SIDE_MENU_MAX_EXPANDED_WIDTH = 296.dp
private const val CHAT_PLACEHOLDER_FADE_STEPS = 16

@Composable
fun ChatScreen(
    component: ChatComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()

    ChatScreenContent(
        modifier = modifier,
        state = state,
        onConversationQueryChange = component::onSearchConversationQueryChanged,
        onModelsSearchQueryChange = component::onModelSearchQueryChanged,
        onStartNewConversation = component::onNewConversationStarted,
        onSelectConversation = component::onConversationOpened,
        onNavigateToModelCatalog = component::onFullModelCatalogOpened,
        onNavigateToAppSettings = component::onAppSettingsOpened,
        onOpenModelSelectorClick = component::onOpenModelSelectorClicked,
        onOpenTuningPanelClick = component::onTuningPanelOpened,
        onPromptChange = component::onPromptChanged,
        onPickAttachmentsClick = component::onAttachmentSelectorOpened,
        onToggleThinkingClick = component::onThinkingToggled,
        onToggleWebSearchClick = component::onWebSearchToggled,
        onSendPrompt = component::onPromptSent,
        onStopGeneration = component::onGenerationStopped,
        onRegenerateLastResponseClick = component::onLastResponseRegenerateRequested,
        onEditConversationTitleClick = component::onRenameConversationDialogOpened,
        onDeleteConversationClick = component::onConversationDeleted,
        onDismissRenameConversationDialog = component::onRenameConversationDialogClosed,
        onConversationNameInputChange = component::onConversationNameInputChange,
        onConfirmConversationRename = component::onConversationRenameConfirmed,
        onDismissErrorDialog = component::onErrorDialogDismissed,
        onDismissChatError = component::onChatErrorDismissed,
        onDismissModelSelector = component::onCloseModelSelectorRequested,
        onModelSelectorTabChange = component::onChangeModelSelectorTab,
        onUseModelClick = component::onModelSelected,
        onUnpinModelClick = component::onModelUnpinned
    )
}

@Composable
private fun ChatScreenContent(
    state: ChatStore.State,
    onConversationQueryChange: (String) -> Unit,
    onModelsSearchQueryChange: (String) -> Unit,
    onStartNewConversation: () -> Unit,
    onSelectConversation: (id: Long) -> Unit,
    onNavigateToModelCatalog: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onOpenModelSelectorClick: () -> Unit,
    onOpenTuningPanelClick: () -> Unit,
    onPromptChange: (String) -> Unit,
    onPickAttachmentsClick: () -> Unit,
    onToggleThinkingClick: () -> Unit,
    onToggleWebSearchClick: () -> Unit,
    onSendPrompt: () -> Unit,
    onStopGeneration: () -> Unit,
    onRegenerateLastResponseClick: () -> Unit,
    onEditConversationTitleClick: (id: Long) -> Unit,
    onDeleteConversationClick: (id: Long) -> Unit,
    onDismissRenameConversationDialog: () -> Unit,
    onConversationNameInputChange: (String) -> Unit,
    onConfirmConversationRename: () -> Unit,
    onDismissErrorDialog: () -> Unit,
    onDismissChatError: (id: Long) -> Unit,
    onDismissModelSelector: () -> Unit,
    onModelSelectorTabChange: (SelectorTab) -> Unit,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onUnpinModelClick: (String, ModelType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ime = WindowInsets.ime

    val scope = rememberCoroutineScope()
    val deviceConfiguration = rememberDeviceConfiguration()
    val sideMenuState = rememberWideNavigationRailState()

    val imeVisible by remember { derivedStateOf { ime.getBottom(density) > 0 } }
    val shouldHideAppBar = deviceConfiguration == DeviceConfiguration.MOBILE_LANDSCAPE && imeVisible
    val sideMenuIsAlwaysExpanded = (deviceConfiguration == DeviceConfiguration.TABLET_LANDSCAPE) ||
        (deviceConfiguration == DeviceConfiguration.DESKTOP)

    Row(modifier = modifier.fillMaxSize()) {
        SideMenu(
            sideMenuState = sideMenuState,
            alwaysExpanded = sideMenuIsAlwaysExpanded,
            conversations = state.conversations,
            activeConversationId = state.activeConversation?.id,
            conversationQuery = state.conversationSearchQuery,
            onStartNewConversation = onStartNewConversation,
            onConversationQueryChange = onConversationQueryChange,
            onClearConversationQuery = { onConversationQueryChange("") },
            onSelectConversation = onSelectConversation,
            onNavigateToModelCatalog = onNavigateToModelCatalog,
            onNavigateToAppSettings = onNavigateToAppSettings,
            onEditConversationTitleClick = onEditConversationTitleClick,
            onDeleteConversationClick = onDeleteConversationClick,
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
                        activeModelName = state.activeModelName
                            ?: stringResource(Res.string.select_model),
                        onNavigationIconClick = { scope.launch { sideMenuState.expand() } },
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
                    .chatPlaceholderGlow(visible = state.messages.isEmpty())
            ) {
                Chat(
                    messages = state.messages,
                    messagesTopPadding = innerPadding.calculateTopPadding(),
                    onRegenerateLastResponseClick = onRegenerateLastResponseClick,
                    onDismissChatError = onDismissChatError,
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
                    prompt = state.prompt,
                    isGenerating = state.isGenerating,
                    thinkingEnabled = state.thinkingEnabled,
                    webSearchEnabled = state.webSearchEnabled,
                    onPromptChange = onPromptChange,
                    onSendPrompt = onSendPrompt,
                    onStopGeneration = onStopGeneration,
                    onToggleThinkingClick = onToggleThinkingClick,
                    onToggleWebSearchClick = onToggleWebSearchClick,
                    onPickAttachmentsClick = onPickAttachmentsClick,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = state.messages.isEmpty(),
                enter = fadeIn(animationSpec = tween(durationMillis = 700)),
                exit = fadeOut(),
                modifier = Modifier
                    .imePadding()
                    .semantics { hideFromAccessibility() },
                label = "EmptyChatPlaceholder"
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.empty_chat_placeholder),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }

    if (state.isModelSelectorOpen) {
        ModelSelectorSheet(
            selectedTab = state.modelSelectorTab,
            modelSearchQuery = state.modelSearchQuery,
            pinnedChatModels = state.pinnedChatModels,
            pinnedImageModels = state.pinnedImageModels,
            onDismissModelSelector = onDismissModelSelector,
            onModelSelectorTabChange = onModelSelectorTabChange,
            onModelsSearchQueryChange = onModelsSearchQueryChange,
            onUseModelClick = onUseModelClick,
            onUnpinModelClick = onUnpinModelClick,
            onNavigateToModelCatalog = onNavigateToModelCatalog
        )
    }

    if (state.renamingConversationId != null) {
        RenameConversationDialog(
            conversationTitle = state.renamingConversationInput,
            onDismissRenameConversationDialog = onDismissRenameConversationDialog,
            onConversationTitleChange = onConversationNameInputChange,
            onConfirmConversationRename = onConfirmConversationRename
        )
    }

    state.errorDialogText?.let { errorUiText ->
        VoidAlertDialog(
            title = stringResource(CoreRes.string.oops),
            message = errorUiText.asString(),
            onDismissRequest = onDismissErrorDialog,
            confirmButton = VoidDialogButton(
                text = stringResource(CoreRes.string.ok),
                onClick = onDismissErrorDialog
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    selectedTab: SelectorTab,
    modelSearchQuery: String,
    pinnedChatModels: List<ChatModel>,
    pinnedImageModels: List<ImageModel>,
    onDismissModelSelector: () -> Unit,
    onModelSelectorTabChange: (SelectorTab) -> Unit,
    onModelsSearchQueryChange: (String) -> Unit,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onUnpinModelClick: (id: String, ModelType) -> Unit,
    onNavigateToModelCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val windowHeightPx = LocalWindowInfo.current.containerSize.height
    val maxContentHeight = remember(density, windowHeightPx) {
        with(density) { (windowHeightPx * 0.8f).toDp() }
    }
    val showEmptyModelListPlaceholder =
        (selectedTab == SelectorTab.ChatModels && pinnedChatModels.isEmpty()) ||
            (selectedTab == SelectorTab.ImageModels && pinnedImageModels.isEmpty())

    ModalBottomSheet(
        sheetState = rememberBottomSheetState(
            initialValue = Hidden,
            enabledValues = setOf(Hidden, Expanded)
        ),
        onDismissRequest = onDismissModelSelector,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
                .animateContentSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "ModelSelectorHeader") {
                ModelSelectorTabSwitcher(
                    selectedTab = selectedTab,
                    onTabSelected = onModelSelectorTabChange
                )
                Spacer(Modifier.height(8.dp))
                VoidSearchBar(
                    query = modelSearchQuery,
                    onQueryChange = onModelsSearchQueryChange,
                    onClearQuery = { onModelsSearchQueryChange("") },
                    placeholder = stringResource(Res.string.search_models)
                )
            }

            if (selectedTab == SelectorTab.ChatModels) {
                items(items = pinnedChatModels, key = { it.id }) { model ->
                    ModelSelectorItem(
                        modelName = model.name,
                        onUseModelClick = {
                            onDismissModelSelector()
                            onUseModelClick(ActiveModelRef.Chat(model.id))
                        },
                        onUnpinModelClick = { onUnpinModelClick(model.id, ModelType.CHAT) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (selectedTab == SelectorTab.ImageModels) {
                items(items = pinnedImageModels, key = { it.id }) { model ->
                    ModelSelectorItem(
                        modelName = model.name,
                        onUseModelClick = {
                            onDismissModelSelector()
                            onUseModelClick(ActiveModelRef.Image(model.id))
                        },
                        onUnpinModelClick = { onUnpinModelClick(model.id, ModelType.IMAGE) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (showEmptyModelListPlaceholder) {
                item(key = "EmptyModelListPlaceholder") {
                    EmptyModelSelectorItem(
                        isSearchQueryBlank = modelSearchQuery.isBlank(),
                        onOpenFullModelCatalog = {
                            onDismissModelSelector()
                            onNavigateToModelCatalog()
                        },
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelSelectorItem(
    modelName: String,
    onUseModelClick: () -> Unit,
    onUnpinModelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(color = MaterialTheme.colorScheme.background)
            .clickable(onClick = onUseModelClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modelName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        IconButton(
            onClick = onUnpinModelClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Default.KeepOff,
                contentDescription = stringResource(Res.string.template_unpin_model, modelName)
            )
        }
    }
}

@Composable
private fun EmptyModelSelectorItem(
    isSearchQueryBlank: Boolean,
    onOpenFullModelCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = if (isSearchQueryBlank) {
                    Icons.Default.KeepOff
                } else {
                    Icons.Default.SearchOff
                },
                contentDescription = null,
                tint = contentColor
            )
            Text(
                text = if (isSearchQueryBlank) {
                    stringResource(Res.string.no_pinned_models)
                } else {
                    stringResource(Res.string.no_models_found)
                },
                color = contentColor
            )
        }
        if (isSearchQueryBlank) {
            TextButton(
                onClick = onOpenFullModelCatalog,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(text = stringResource(Res.string.open_model_catalog))
                Icon(
                    imageVector = Icons.AutoMirrored.ChevronRight,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ModelSelectorTabSwitcher(
    selectedTab: SelectorTab,
    onTabSelected: (SelectorTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodySmall
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.chat_models),
            style = textStyle,
            color = textColor
        )
        Switch(
            checked = selectedTab is SelectorTab.ImageModels,
            onCheckedChange = { checked ->
                val targetTab = if (checked) SelectorTab.ImageModels else SelectorTab.ChatModels
                onTabSelected(targetTab)
            },
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.surface
            ),
            thumbContent = {
                Icon(
                    imageVector = if (selectedTab is SelectorTab.ChatModels) {
                        Icons.Default.Chat
                    } else {
                        Icons.Default.Image
                    },
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        )
        Text(
            text = stringResource(Res.string.image_models),
            style = textStyle,
            color = textColor
        )
    }
}

@Composable
private fun RenameConversationDialog(
    conversationTitle: String,
    onConversationTitleChange: (String) -> Unit,
    onDismissRenameConversationDialog: () -> Unit,
    onConfirmConversationRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRenameConversationDialog) {
        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraExtraLarge
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.rename_conversation_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.rename_conversation_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            VoidTextField(
                value = conversationTitle,
                onValueChange = onConversationTitleChange,
                placeholder = {
                    Text(
                        text = stringResource(Res.string.placeholder_new_conversation_title),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.heightIn(min = 56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onConfirmConversationRename,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(text = stringResource(Res.string.rename))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDismissRenameConversationDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        }
    }
}

@Composable
private fun Chat(
    messages: List<Message>,
    messagesTopPadding: Dp,
    onRegenerateLastResponseClick: () -> Unit,
    onDismissChatError: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = messagesTopPadding,
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        )
    ) {
        itemsIndexed(
            items = messages,
            key = { _, item -> item.id },
            contentType = { _, item -> item::class }
        ) { index, message ->
            when (message) {
                is Message.Completion -> CompletionMessageItem(
                    completion = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateResponse = onRegenerateLastResponseClick,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Error -> ErrorMessageItem(
                    error = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateLastResponseClick = onRegenerateLastResponseClick,
                    onDismissChatError = onDismissChatError,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.ImageLoading,
                is Message.Images,
                -> {
                }

                // TODO: Images are not supported yet

                is Message.Interrupted -> InterruptedMessageItem(
                    message = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateResponse = onRegenerateLastResponseClick,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Prompt -> PromptMessageItem(
                    prompt = message,
                    modifier = Modifier.animateItem(placementSpec = null)
                )
            }
        }
    }
}

@Composable
private fun PromptComposer(
    prompt: String,
    isGenerating: Boolean,
    thinkingEnabled: Boolean,
    webSearchEnabled: Boolean,
    onSendPrompt: () -> Unit,
    onStopGeneration: () -> Unit,
    onPromptChange: (String) -> Unit,
    onToggleThinkingClick: () -> Unit,
    onToggleWebSearchClick: () -> Unit,
    onPickAttachmentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp)
    ) {
        PromptTextField(
            prompt = prompt,
            onPromptChange = onPromptChange
        )

        Row {
            AttachmentButton(onClick = onPickAttachmentsClick)
            GenerationOption(
                icon = Icons.Default.Lightbulb,
                label = stringResource(Res.string.option_think),
                enabled = thinkingEnabled,
                onClick = onToggleThinkingClick
            )
            Spacer(Modifier.width(4.dp))
            GenerationOption(
                icon = Icons.Default.Explore,
                label = stringResource(Res.string.option_search),
                enabled = webSearchEnabled,
                onClick = onToggleWebSearchClick
            )
            Spacer(Modifier.weight(1f))
            SendPromptButton(
                isGenerating = isGenerating,
                isPromptBlank = prompt.isBlank(),
                onStopGeneration = onStopGeneration,
                onSendPrompt = onSendPrompt
            )
        }
    }
}

@Composable
private fun SendPromptButton(
    isGenerating: Boolean,
    isPromptBlank: Boolean,
    onStopGeneration: () -> Unit,
    onSendPrompt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = if (isGenerating) onStopGeneration else onSendPrompt,
        enabled = isGenerating || !isPromptBlank,
        colors = IconButtonDefaults.filledIconButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Crossfade(
            targetState = isGenerating,
            label = "SendPromptButtonContent"
        ) { generating ->
            Icon(
                imageVector = if (generating) {
                    Icons.Default.Stop
                } else {
                    Icons.Default.ArrowUpward
                },
                contentDescription = if (generating) {
                    stringResource(Res.string.desc_stop_generation)
                } else {
                    stringResource(Res.string.desc_send_message)
                }
            )
        }
    }
}

@Composable
private fun AttachmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = Icons.Default.Attach,
            contentDescription = stringResource(Res.string.desc_attach_files)
        )
    }
}

@Composable
private fun PromptTextField(
    prompt: String,
    onPromptChange: (String) -> Unit,
) {
    TextField(
        value = prompt,
        onValueChange = onPromptChange,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        maxLines = PROMPT_MAX_LINES,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = stringResource(Res.string.placeholder_empty_prompt),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
private fun ChatTopAppBar(
    isSideMenuAlwaysExpanded: Boolean,
    activeModelName: String,
    onNavigationIconClick: () -> Unit,
    onOpenModelSelectorClick: () -> Unit,
    onOpenTuningPanelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        navigationIcon = {
            if (!isSideMenuAlwaysExpanded) {
                AppBarNavigationButton(onClick = onNavigationIconClick)
            }
        },
        title = { ActiveModelAppBarTitle(activeModelName, onClick = onOpenModelSelectorClick) },
        actions = { TuneModelButton(onClick = onOpenTuningPanelClick) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(end = 6.dp)
    )
}

@Composable
private fun AppBarNavigationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.MenuOpen,
            contentDescription = stringResource(Res.string.desc_open_side_menu)
        )
    }
}

@Composable
private fun TuneModelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.ExtraSmallIconSpacing))
        Text(text = stringResource(Res.string.tune_model))
    }
}

@Composable
private fun ActiveModelAppBarTitle(
    activeModelName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.template_desc_active_model, activeModelName)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                role = Role.Button,
                indication = null,
                interactionSource = null
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .clearAndSetSemantics { this.contentDescription = contentDescription }
    ) {
        AnimatedContent(
            targetState = activeModelName,
            transitionSpec = {
                slideInHorizontally() + fadeIn() togetherWith
                    slideOutHorizontally { it / 2 } + fadeOut()
            },
            label = "AppBarModelName",
            modifier = Modifier.weight(1f, false)
        ) { modelName ->
            Text(
                text = modelName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PromptMessageItem(
    prompt: Message.Prompt,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.End)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp)
    ) {
        SelectionContainer {
            Text(
                text = prompt.query,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InterruptedMessageItem(
    message: Message.Interrupted,
    isLastMessage: Boolean,
    onRegenerateResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectionContainer {
            Markdown(
                content = message.text ?: stringResource(Res.string.no_text_was_generated),
                typography = chatMarkdownTypography(),
                imageTransformer = Coil3ImageTransformerImpl,
                components = rememberMarkdownComponents()
            )
        }
        AnimatedVisibility(
            visible = isLastMessage,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            label = "RetryInterrupted"
        ) {
            TextButton(
                onClick = onRegenerateResponse,
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(Res.string.textbutton_retry_interupted_response),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompletionMessageItem(
    completion: Message.Completion,
    isLastMessage: Boolean,
    onRegenerateResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val layoutDirection = LocalLayoutDirection.current

    val reasoningMdState = rememberMarkdownState(completion.reasoning.orEmpty(), retainState = true)
    val completionMdState = rememberMarkdownState(completion.text, retainState = true)

    var reasoningExpanded by rememberSaveable(completion.id) { mutableStateOf(false) }
    val reasoningChevronRotation = animateFloatAsState(
        targetValue = if (reasoningExpanded) {
            if (layoutDirection == LayoutDirection.Rtl) -90f else 90f
        } else {
            0f
        }
    )

    Column(modifier = modifier) {
        if (completion.reasoning != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        onClick = { reasoningExpanded = !reasoningExpanded },
                        indication = null,
                        interactionSource = null,
                        role = Role.Button
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    text = stringResource(
                        if (reasoningExpanded) {
                            Res.string.hide_reasoning
                        } else {
                            Res.string.show_reasoning
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.animateContentSize(alignment = Alignment.CenterEnd)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = reasoningChevronRotation.value
                    }
                )
            }
            AnimatedVisibility(
                visible = reasoningExpanded,
                label = "ReasoningText"
            ) {
                SelectionContainer {
                    Markdown(
                        markdownState = reasoningMdState,
                        typography = chatMarkdownTypography(),
                        imageTransformer = Coil3ImageTransformerImpl,
                        components = rememberMarkdownComponents(),
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (completion.text.isNotEmpty()) {
            SelectionContainer {
                Markdown(
                    markdownState = completionMdState,
                    typography = chatMarkdownTypography(),
                    imageTransformer = Coil3ImageTransformerImpl,
                    components = rememberMarkdownComponents()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = completion.isStreaming,
            label = "CompletionMessageItemContent"
        ) { isStreaming ->
            if (isStreaming) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LoadingIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = stringResource(Res.string.loading_generation),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = (-8).dp)
                ) {
                    if (isLastMessage) {
                        ResponseActionButton(
                            icon = Icons.AutoMirrored.Restart,
                            onClick = onRegenerateResponse,
                            contentDescription = stringResource(
                                Res.string.desc_regenerate_last_response
                            )
                        )
                    }
                    ResponseActionButton(
                        icon = Icons.Default.ContentCopy,
                        onClick = { clipboardManager.setText(AnnotatedString(completion.text)) },
                        contentDescription = stringResource(Res.string.desc_copy_last_response)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponseActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(18.dp)
                .pointerHoverIcon(PointerIcon.Hand)
        )
    }
}

@Composable
private fun ErrorMessageItem(
    error: Message.Error,
    isLastMessage: Boolean,
    onRegenerateLastResponseClick: () -> Unit,
    onDismissChatError: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorTitle = stringResource(
        when (error.reason) {
            ErrorReason.NETWORK -> Res.string.error_title_network
            ErrorReason.SERVER_INTERNAL -> Res.string.error_title_server
            ErrorReason.INSUFFICIENT_CREDITS -> Res.string.error_title_credits
            ErrorReason.RATE_LIMITED -> Res.string.error_title_rate_limit
            ErrorReason.UNAUTHORIZED -> Res.string.error_title_unauthorized
            ErrorReason.UNEXPECTED -> Res.string.error_title_unexpected
        }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.large
            )
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = errorTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        error.rawMessage?.let { rawMessage ->
            Text(
                text = stringResource(Res.string.error_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    text = rawMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                )
            }
        }
        Row {
            AnimatedVisibility(
                visible = isLastMessage,
                label = "RegenerateResponseTextButton"
            ) {
                TextButton(
                    onClick = onRegenerateLastResponseClick,
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp).pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Restart,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(Res.string.retry))
                }
            }
            TextButton(
                onClick = { onDismissChatError(error.id) },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(Res.string.dismiss))
            }
        }
    }
}

@Composable
private fun GenerationOption(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = enabled,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = CircleShape,
        contentPadding = PaddingValues(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
private fun SideMenu(
    sideMenuState: WideNavigationRailState,
    alwaysExpanded: Boolean,
    conversations: List<Conversation>,
    onStartNewConversation: () -> Unit,
    activeConversationId: Long?,
    conversationQuery: String,
    onConversationQueryChange: (String) -> Unit,
    onClearConversationQuery: () -> Unit,
    onSelectConversation: (id: Long) -> Unit,
    onNavigateToModelCatalog: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onEditConversationTitleClick: (id: Long) -> Unit,
    onDeleteConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val collapseSideMenu: () -> Unit = remember(scope, sideMenuState) {
        { scope.launch { sideMenuState.collapse() } }
    }
    val colors = WideNavigationRailDefaults.colors(
        modalContainerColor = MaterialTheme.colorScheme.surface,
        modalContentColor = MaterialTheme.colorScheme.onBackground
    )

    if (alwaysExpanded) {
        WideNavigationRail(
            state = rememberWideNavigationRailState(WideNavigationRailValue.Expanded),
            modifier = modifier
                .widthIn(max = SIDE_MENU_MAX_EXPANDED_WIDTH)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 32.dp),
            colors = colors,
            shape = RoundedCornerShape(24.dp)
        ) {
            SideMenuContent(
                conversations = conversations,
                activeConversationId = activeConversationId,
                conversationSearchQuery = conversationQuery,
                onStartNewConversation = {
                    collapseSideMenu()
                    onStartNewConversation()
                },
                onConversationQueryChange = onConversationQueryChange,
                onClearConversationQuery = onClearConversationQuery,
                onSelectConversation = onSelectConversation,
                onNavigateToModelCatalog = onNavigateToModelCatalog,
                onNavigateToAppSettings = onNavigateToAppSettings,
                onCloseSideMenuClicked = {},
                onEditConversationTitleClick = onEditConversationTitleClick,
                onDeleteConversationClick = onDeleteConversationClick
            )
        }
    } else {
        ModalWideNavigationRail(
            state = sideMenuState,
            colors = colors,
            expandedShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            contentPadding = PaddingValues(start = 16.dp),
            hideOnCollapse = true
        ) {
            SideMenuContent(
                showHeader = true,
                conversations = conversations,
                activeConversationId = activeConversationId,
                onStartNewConversation = {
                    collapseSideMenu()
                    onStartNewConversation()
                },
                conversationSearchQuery = conversationQuery,
                onConversationQueryChange = onConversationQueryChange,
                onClearConversationQuery = onClearConversationQuery,
                onSelectConversation = onSelectConversation,
                onNavigateToModelCatalog = {
                    collapseSideMenu()
                    onNavigateToModelCatalog()
                },
                onNavigateToAppSettings = {
                    collapseSideMenu()
                    onNavigateToAppSettings()
                },
                onCloseSideMenuClicked = collapseSideMenu,
                onEditConversationTitleClick = onEditConversationTitleClick,
                onDeleteConversationClick = onDeleteConversationClick
            )
        }
    }
}

@Composable
private fun SideMenuHeader(
    modifier: Modifier = Modifier,
    onCloseSideMenuClicked: (() -> Unit),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Logo,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(Res.string.void),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { hideFromAccessibility() }
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onCloseSideMenuClicked,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.LeftPanelClose,
                contentDescription = stringResource(Res.string.desc_collapse_side_menu)
            )
        }
    }
}

@Composable
private fun SideMenuContent(
    conversations: List<Conversation>,
    activeConversationId: Long?,
    onStartNewConversation: () -> Unit,
    conversationSearchQuery: String,
    onConversationQueryChange: (String) -> Unit,
    onClearConversationQuery: () -> Unit,
    onSelectConversation: (id: Long) -> Unit,
    onNavigateToModelCatalog: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onCloseSideMenuClicked: () -> Unit,
    onEditConversationTitleClick: (id: Long) -> Unit,
    onDeleteConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showHeader) {
            item(key = "Header") {
                SideMenuHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    onCloseSideMenuClicked = onCloseSideMenuClicked
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "NewChat") {
            SideMenuItem(
                icon = Icons.Default.Stylus,
                label = stringResource(Res.string.new_chat),
                onClick = onStartNewConversation
            )
        }
        item(key = "ModelCatalog") {
            SideMenuItem(
                icon = Icons.Default.Awesome,
                label = stringResource(Res.string.model_catalog),
                onClick = onNavigateToModelCatalog
            )
        }
        item(key = "Settings") {
            SideMenuItem(
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.settings),
                onClick = onNavigateToAppSettings
            )
        }

        item(key = "SearchConversations") {
            Spacer(Modifier.height(24.dp))
            VoidSearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = conversationSearchQuery,
                onQueryChange = onConversationQueryChange,
                onClearQuery = onClearConversationQuery,
                placeholder = stringResource(Res.string.placeholder_search_chats)
            )
        }

        items(items = conversations, key = { it.id }) { conversation ->
            ConversationItem(
                conversation = conversation,
                activeConversationId = activeConversationId,
                onSelectConversation = { conversationId ->
                    onCloseSideMenuClicked()
                    onSelectConversation(conversationId)
                },
                onEditConversationTitleClick = onEditConversationTitleClick,
                onDeleteConversationClick = onDeleteConversationClick,
                modifier = Modifier.animateItem()
            )
        }

        if (conversations.isEmpty()) {
            item(key = "EmptyConversations") {
                Text(
                    text = stringResource(
                        if (conversationSearchQuery.isEmpty()) {
                            Res.string.placeholder_no_conversations
                        } else {
                            Res.string.placeholder_no_conversations_found
                        }
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    activeConversationId: Long?,
    onSelectConversation: (id: Long) -> Unit,
    onEditConversationTitleClick: (id: Long) -> Unit,
    onDeleteConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = conversation.id == activeConversationId
    val title = conversation.title ?: stringResource(Res.string.untitled_conversation)
    var actionMenuExpanded by remember { mutableStateOf(false) }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val renameContentDescription = stringResource(Res.string.template_rename, title)
    val deleteContentDescription = stringResource(Res.string.template_delete, title)

    Row(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = { onSelectConversation(conversation.id) })
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = { actionMenuExpanded = true },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(
                    Res.string.template_desc_more_conv_actions,
                    title
                ),
                tint = contentColor
            )
            DropdownMenu(
                expanded = actionMenuExpanded,
                onDismissRequest = { actionMenuExpanded = false },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .semantics { contentDescription = renameContentDescription },
                    text = { Text(text = stringResource(Res.string.rename)) },
                    onClick = {
                        actionMenuExpanded = false
                        onEditConversationTitleClick(conversation.id)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Stylus,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .semantics { contentDescription = deleteContentDescription },
                    text = { Text(text = stringResource(Res.string.delete)) },
                    onClick = {
                        actionMenuExpanded = false
                        onDeleteConversationClick(conversation.id)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SideMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun rememberMarkdownComponents() = remember {
    markdownComponents(
        codeBlock = {
            MarkdownHighlightedCodeBlock(
                content = it.content,
                node = it.node,
                showHeader = true
            )
        },
        codeFence = {
            MarkdownHighlightedCodeFence(
                content = it.content,
                node = it.node,
                showHeader = true
            )
        }
    )
}

@Composable
private fun Modifier.chatPlaceholderGlow(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    heightFraction: Float = 0.95f,
    horizontalFadeFraction: Float = 0.25f,
    bottomOffset: Dp = 0.dp,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    ),
): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        progress.animateTo(if (visible) 1f else 0f, animationSpec)
    }

    val brush = remember(color) {
        Brush.verticalGradient(
            colorStops = Array(CHAT_PLACEHOLDER_FADE_STEPS + 1) { i ->
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                t to color.copy(alpha = SmoothStepEasing(t))
            }
        )
    }

    val horizontalMask = remember(horizontalFadeFraction) {
        val f = horizontalFadeFraction.coerceIn(0f, 0.5f)
        val stops = buildList {
            for (i in 0..CHAT_PLACEHOLDER_FADE_STEPS) {
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                add(t * f to Color.Black.copy(alpha = SmoothStepEasing(t)))
            }
            for (i in CHAT_PLACEHOLDER_FADE_STEPS downTo 0) {
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                add((1f - t * f) to Color.Black.copy(alpha = SmoothStepEasing(t)))
            }
        }
        Brush.horizontalGradient(colorStops = stops.toTypedArray())
    }

    val bottomMask = remember {
        val stops = Array(CHAT_PLACEHOLDER_FADE_STEPS + 1) { i ->
            val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
            (0.85f + 0.15f * t) to Color.Black.copy(alpha = SmoothStepEasing(1f - t))
        }
        Brush.verticalGradient(colorStops = arrayOf(0f to Color.Black, *stops))
    }

    return drawWithCache {
        val areaSize = Size(
            width = size.width,
            height = (size.height - bottomOffset.toPx()).coerceAtLeast(0f)
        )
        val glowHeight = areaSize.height * heightFraction

        onDrawBehind {
            val p = progress.value
            if (p <= 0f || areaSize.height <= 0f) return@onDrawBehind

            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(Offset.Zero, areaSize), Paint())

                translate(top = areaSize.height - glowHeight * p) {
                    drawRect(
                        brush = brush,
                        size = Size(areaSize.width, glowHeight),
                        alpha = p
                    )
                }
                drawRect(
                    brush = horizontalMask,
                    size = areaSize,
                    blendMode = BlendMode.DstIn
                )
                drawRect(
                    brush = bottomMask,
                    size = areaSize,
                    blendMode = BlendMode.DstIn
                )

                canvas.restore()
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun ChatScreenPreview() {
    VoidTheme {
        ChatScreenContent(
            state = ChatStore.State(
                prompt = "",
                isModelSelectorOpen = true,
                thinkingEnabled = true,
                webSearchEnabled = false,
                messages = listOf(
                    Message.Prompt(id = 0, query = "Prompt message"),
                    Message.Completion(
                        id = 1,
                        reasoning = "Reasoning text from AI is here",
                        text = "This is a generating response from AI. One, two, three, fo",
                        isStreaming = true
                    ),
                    Message.Prompt(id = 2, query = "Prompt"),
                    Message.Interrupted(
                        id = 4,
                        text = "Interrupted generati"
                    )
                ),
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
                activeConversation = Conversation(
                    id = 2,
                    title = "Title of selected one",
                    createdAt = 222222222L,
                    updatedAt = 222222222L
                )
            ),
            onConversationQueryChange = {},
            onSelectConversation = {},
            onNavigateToAppSettings = {},
            onNavigateToModelCatalog = {},
            onOpenModelSelectorClick = {},
            onOpenTuningPanelClick = {},
            onPromptChange = {},
            onSendPrompt = {},
            onToggleWebSearchClick = {},
            onToggleThinkingClick = {},
            onPickAttachmentsClick = {},
            onStopGeneration = {},
            onRegenerateLastResponseClick = {},
            onEditConversationTitleClick = {},
            onDeleteConversationClick = {},
            onConfirmConversationRename = {},
            onConversationNameInputChange = {},
            onDismissRenameConversationDialog = {},
            onDismissErrorDialog = {},
            onStartNewConversation = {},
            onDismissChatError = {},
            onDismissModelSelector = {},
            onModelSelectorTabChange = {},
            onUnpinModelClick = { _, _ -> },
            onUseModelClick = {},
            onModelsSearchQueryChange = {}
        )
    }
}
