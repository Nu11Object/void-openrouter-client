package com.nullo.voidapp.feature.catalog.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.designsystem.component.BackTopAppBar
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.component.VoidSearchBar
import com.nullo.voidapp.core.designsystem.component.VoidSnackbarHost
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Chat
import com.nullo.voidapp.core.designsystem.icon.default.Check
import com.nullo.voidapp.core.designsystem.icon.default.CloudSync
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.core.designsystem.icon.default.Keep
import com.nullo.voidapp.core.designsystem.icon.default.Lightbulb
import com.nullo.voidapp.core.designsystem.icon.default.Robot
import com.nullo.voidapp.core.designsystem.icon.default.Stylus
import com.nullo.voidapp.core.designsystem.icon.default.Vision
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.bottomFade
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.catalog.presentation.component.CatalogComponent
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State.CatalogTab
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.core.utils.generated.resources.template_dismiss_with_timer
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.auto_sync_description
import voidapp.feature.catalog.generated.resources.chat_models
import voidapp.feature.catalog.generated.resources.image_models
import voidapp.feature.catalog.generated.resources.model_catalog
import voidapp.feature.catalog.generated.resources.no_models_found
import voidapp.feature.catalog.generated.resources.per_1m_tokens
import voidapp.feature.catalog.generated.resources.pin
import voidapp.feature.catalog.generated.resources.reasoning
import voidapp.feature.catalog.generated.resources.refresh
import voidapp.feature.catalog.generated.resources.search_models
import voidapp.feature.catalog.generated.resources.syncing
import voidapp.feature.catalog.generated.resources.template_context_length
import voidapp.feature.catalog.generated.resources.unpin
import voidapp.feature.catalog.generated.resources.use_now
import voidapp.feature.catalog.generated.resources.verbose
import voidapp.feature.catalog.generated.resources.vision

private val innerContentPadding = 16.dp

@Composable
fun CatalogScreen(
    catalogComponent: CatalogComponent,
    modifier: Modifier = Modifier,
) {
    val model by catalogComponent.model.subscribeAsState()

    CatalogScreenContent(
        state = model,
        onBackClick = catalogComponent::onBackClicked,
        onSearchQueryChange = catalogComponent::onSearchQueryChanged,
        onClearSearchQuery = catalogComponent::onSearchQueryCleared,
        onRefreshCatalogClick = catalogComponent::onRefreshCatalogClicked,
        onToggleVerboseInfo = catalogComponent::onVerboseInfoToggled,
        onTabSelected = catalogComponent::onTabSelected,
        onUseModelClick = catalogComponent::onUseModelClicked,
        onPinModelClick = catalogComponent::onPinModelClicked,
        onDismissMessage = catalogComponent::onMessageDismissed,
        onDismissError = catalogComponent::onErrorDismissed,
        modifier = modifier
    )
}

@Composable
private fun CatalogScreenContent(
    state: CatalogStore.State,
    onBackClick: () -> Unit,
    onSearchQueryChange: (query: String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onRefreshCatalogClick: () -> Unit,
    onToggleVerboseInfo: (Boolean) -> Unit,
    onTabSelected: (CatalogTab) -> Unit,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    onDismissMessage: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceConfiguration = rememberDeviceConfiguration()
    val isPortrait = remember(deviceConfiguration) {
        deviceConfiguration == DeviceConfiguration.MOBILE_PORTRAIT ||
            deviceConfiguration == DeviceConfiguration.TABLET_PORTRAIT
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            BackTopAppBar(
                title = stringResource(Res.string.model_catalog),
                onBackClick = onBackClick,
                actions = {
                    if (!isPortrait) {
                        CatalogTabSwitcher(
                            selectedTab = state.selectedTab,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            VoidSnackbarHost(
                message = state.message,
                action = {
                    Button(
                        onClick = onDismissMessage,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text(
                            text = stringResource(
                                CoreRes.string.template_dismiss_with_timer,
                                state.messageDismissalTimer
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isPortrait) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoidSearchBar(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onClearQuery = onClearSearchQuery,
                        placeholder = stringResource(Res.string.search_models),
                        modifier = Modifier.padding(horizontal = innerContentPadding)
                    )

                    ControlsSection(
                        showVerboseInfo = state.showVerboseInfo,
                        onToggleVerboseInfo = onToggleVerboseInfo,
                        onRefreshCatalogClick = onRefreshCatalogClick,
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = innerContentPadding)
                    )

                    ModelsListSection(
                        state = state,
                        onUseModelClick = onUseModelClick,
                        onPinModelClick = onPinModelClick
                    )
                }

                CatalogFloatingToolbar(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(3f)
                ) {
                    VoidSearchBar(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onClearQuery = onClearSearchQuery,
                        placeholder = stringResource(Res.string.search_models),
                        modifier = Modifier.padding(horizontal = innerContentPadding)
                    )

                    ControlsSection(
                        showVerboseInfo = state.showVerboseInfo,
                        onToggleVerboseInfo = onToggleVerboseInfo,
                        onRefreshCatalogClick = onRefreshCatalogClick,
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = innerContentPadding)
                    )
                }

                ModelsListSection(
                    state = state,
                    onUseModelClick = onUseModelClick,
                    onPinModelClick = onPinModelClick,
                    bottomFadeFraction = 0.1f,
                    modifier = Modifier.weight(5f)
                )
            }
        }
    }

    state.error?.let { error ->
        VoidAlertDialog(
            title = stringResource(CoreRes.string.oops),
            message = error.asString(),
            onDismissRequest = onDismissError,
            confirmButton = VoidDialogButton(
                text = stringResource(CoreRes.string.ok),
                onClick = onDismissError
            )
        )
    }
}

@Composable
private fun ModelsListSection(
    state: CatalogStore.State,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bottomFadeFraction: Float = 0.2f,
) {
    val isListEmpty = when (state.selectedTab) {
        is CatalogTab.ChatModels -> state.chatModels.isEmpty()
        is CatalogTab.ImageModels -> state.imageModels.isEmpty()
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 296.dp),
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp
                )
            )
            .bottomFade(heightFraction = bottomFadeFraction),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 128.dp
        ),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isListEmpty) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyCatalogItem(isDbEmpty = state.isDbEmpty)
                }
            }
        } else {
            when (state.selectedTab) {
                is CatalogTab.ChatModels -> {
                    items(
                        state.chatModels,
                        key = { it.id }
                    ) {
                        ChatModelItem(
                            chatModel = it,
                            verbose = state.showVerboseInfo,
                            onUseModelClick = onUseModelClick,
                            onPinModelClick = onPinModelClick,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                is CatalogTab.ImageModels -> {
                    items(
                        state.imageModels,
                        key = { it.id }
                    ) {
                        ImageModelItem(
                            imageModel = it,
                            verbose = state.showVerboseInfo,
                            onUseModelClick = onUseModelClick,
                            onPinModelClick = onPinModelClick,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCatalogItem(
    isDbEmpty: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isDbEmpty) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.auto_sync_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = stringResource(Res.string.no_models_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatModelItem(
    chatModel: ChatModel,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    verbose: Boolean,
    modifier: Modifier = Modifier,
) = ModelCard(modifier = modifier) {
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Text(text = chatModel.name, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedContent(
            targetState = verbose,
            label = "ContextInfo"
        ) { isVerbose ->
            if (isVerbose) {
                Text(
                    text = stringResource(Res.string.per_1m_tokens),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            } else {
                chatModel.contextLength?.let { context ->
                    Text(
                        text = stringResource(
                            Res.string.template_context_length,
                            context.toCompactString()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.Stylus,
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
        Text(
            text = chatModel.pricing.promptPerMillion.toPriceString(),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
        Icon(
            imageVector = Icons.Default.Robot,
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
        Text(
            text = chatModel.pricing.completionPerMillion.toPriceString(),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
    }
    Spacer(Modifier.height(4.dp))
    AnimatedVisibility(visible = verbose) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chatModel.description.takeIf { it.isNotEmpty() }?.let { desc ->
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chatModel.contextLength?.let { context ->
                    OutlineTag(
                        text = stringResource(
                            Res.string.template_context_length,
                            context.toCompactString()
                        )
                    )
                }
                if (chatModel.supportsReasoning) {
                    OutlineTag(
                        icon = Icons.Default.Lightbulb,
                        text = stringResource(Res.string.reasoning)
                    )
                }
                if (chatModel.supportsVision) {
                    OutlineTag(
                        icon = Icons.Default.Vision,
                        text = stringResource(Res.string.vision)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    ModelInteractionButtons(
        onUseModelClick = { onUseModelClick(ActiveModelRef.Chat(chatModel.id)) },
        onPinModelClick = { onPinModelClick(chatModel.id, ModelType.CHAT, !chatModel.pinned) },
        isModelPinned = chatModel.pinned,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ImageModelItem(
    imageModel: ImageModel,
    verbose: Boolean,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = ModelCard(modifier = modifier) {
    Text(text = imageModel.name)
    AnimatedVisibility(visible = verbose && imageModel.description.isNotEmpty()) {
        Column {
            Spacer(Modifier.height(6.dp))
            Text(
                modifier = Modifier.padding(vertical = 6.dp),
                text = imageModel.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
    Spacer(Modifier.height(6.dp))
    ModelInteractionButtons(
        onUseModelClick = { onUseModelClick(ActiveModelRef.Image(imageModel.id)) },
        onPinModelClick = {
            onPinModelClick(imageModel.id, ModelType.IMAGE, !imageModel.pinned)
        },
        isModelPinned = imageModel.pinned,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ModelCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(innerContentPadding),
        content = content
    )
}

@Composable
private fun ModelInteractionButtons(
    onUseModelClick: () -> Unit,
    onPinModelClick: () -> Unit,
    isModelPinned: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onUseModelClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceDim,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .weight(1f)
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(text = stringResource(Res.string.use_now))
        }
        Button(
            onClick = onPinModelClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isModelPinned) {
                    MaterialTheme.colorScheme.surfaceDim
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (isModelPinned) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            ),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier
                .weight(1f)
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Default.Keep,
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(
                    if (isModelPinned) {
                        Res.string.unpin
                    } else {
                        Res.string.pin
                    }
                ),
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogFloatingToolbar(
    selectedTab: CatalogTab,
    onTabSelected: (CatalogTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    ) {
        val isChatSelected = selectedTab is CatalogTab.ChatModels
        val isImageSelected = selectedTab is CatalogTab.ImageModels
        val shapes = remember {
            ToggleButtonShapes(
                shape = CircleShape,
                pressedShape = RoundedCornerShape(8.dp),
                checkedShape = CircleShape
            )
        }
        val colors = ToggleButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )

        ToggleButton(
            checked = isChatSelected,
            onCheckedChange = { onTabSelected(CatalogTab.ChatModels) },
            shapes = shapes,
            colors = colors,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(Res.string.chat_models))
        }

        ToggleButton(
            checked = isImageSelected,
            onCheckedChange = { onTabSelected(CatalogTab.ImageModels) },
            shapes = shapes,
            colors = colors,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(Res.string.image_models))
        }
    }
}

@Composable
private fun CatalogTabSwitcher(
    selectedTab: CatalogTab,
    onTabSelected: (CatalogTab) -> Unit,
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
            checked = selectedTab is CatalogTab.ImageModels,
            onCheckedChange = { checked ->
                onTabSelected(
                    if (checked) CatalogTab.ImageModels else CatalogTab.ChatModels
                )
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
                    imageVector = if (selectedTab is CatalogTab.ChatModels) {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlsSection(
    showVerboseInfo: Boolean,
    onToggleVerboseInfo: (Boolean) -> Unit,
    onRefreshCatalogClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { if (!isLoading) onRefreshCatalogClick() },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Crossfade(targetState = isLoading) { loading ->
                if (loading) {
                    LoadingIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(
                    if (isLoading) {
                        Res.string.syncing
                    } else {
                        Res.string.refresh
                    }
                ),
                modifier = Modifier.animateContentSize()
            )
        }
        Spacer(Modifier.weight(1f))
        Switch(
            checked = showVerboseInfo,
            onCheckedChange = onToggleVerboseInfo,
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            ),
            thumbContent = {
                if (showVerboseInfo) {
                    Icon(
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(Res.string.verbose),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun OutlineTag(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun BigDecimal.toPriceString(): String {
    val plain = this.toPlainString()
    if (!plain.contains('.')) return "$$plain"

    val trimmed = plain.trimEnd('0').trimEnd('.')
    return "$" + trimmed.ifEmpty { "0" }
}

fun Int.toCompactString(): String = when {
    this >= 1_000_000 -> formatCompact(this, 1_000_000, "M")
    this >= 10_000 -> formatCompact(this, 1_000, "K")
    else -> this.toString()
}

private fun formatCompact(value: Int, divisor: Int, suffix: String): String {
    val whole = value / divisor
    val remainder = value % divisor
    val tenth = (remainder * 10) / divisor

    return if (tenth == 0) {
        "$whole$suffix"
    } else {
        "$whole.$tenth$suffix"
    }
}

@PreviewLightDark
@Composable
private fun CatalogScreenPreview() {
    VoidTheme {
        CatalogScreenContent(
            state = CatalogStore.State(
                chatModels = buildList {
                    repeat(30) {
                        val model = ChatModel(
                            id = "id$it",
                            name = "Model Name",
                            description = "Super cool model! Try it right now or pin idk...",
                            createdAtTimestamp = 10000000,
                            contextLength = 256000,
                            maxCompletionTokens = 1000000,
                            inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                            outputModalities = setOf(Modality.TEXT),
                            supportedFeatures = setOf(ModelFeature.THINKING),
                            supportedTuningParameters = setOf(),
                            defaultTuningParameters = DefaultTuningParameters(),
                            pricing = ModelPricing(
                                promptPerMillion = 3.toBigDecimal(),
                                completionPerMillion = (15.5).toBigDecimal()
                            ),
                            pinned = true
                        )
                        add(model)
                    }
                }
            ),
            onBackClick = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onRefreshCatalogClick = {},
            onToggleVerboseInfo = {},
            onTabSelected = {},
            onPinModelClick = { _, _, _ -> },
            onUseModelClick = {},
            onDismissMessage = {},
            onDismissError = {}
        )
    }
}
