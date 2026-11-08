package com.nullo.voidapp.feature.chat.presentation.ui.modelselector

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.component.VoidSearchBar
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.State.SelectorTab
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.search_models

@Immutable
internal data class ModelSelectorActions(
    val onDismiss: () -> Unit = {},
    val onTabChange: (SelectorTab) -> Unit = {},
    val onSearchQueryChange: (String) -> Unit = {},
    val onUseModel: (ActiveModelRef) -> Unit = {},
    val onUnpinModel: (id: String, ModelType) -> Unit = { _, _ -> },
    val onNavigateToModelCatalog: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelSelectorSheet(
    selectedTab: SelectorTab,
    modelSearchQuery: String,
    pinnedChatModels: List<ChatModel>,
    pinnedImageModels: List<ImageModel>,
    actions: ModelSelectorActions,
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
        onDismissRequest = actions.onDismiss,
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
                    onTabSelected = actions.onTabChange
                )
                Spacer(Modifier.height(8.dp))
                VoidSearchBar(
                    query = modelSearchQuery,
                    onQueryChange = actions.onSearchQueryChange,
                    onClearQuery = { actions.onSearchQueryChange("") },
                    placeholder = stringResource(Res.string.search_models)
                )
            }

            if (selectedTab == SelectorTab.ChatModels) {
                items(items = pinnedChatModels, key = { it.id }) { model ->
                    ModelSelectorItem(
                        modelName = model.name,
                        onUseModelClick = {
                            actions.onDismiss()
                            actions.onUseModel(ActiveModelRef.Chat(model.id))
                        },
                        onUnpinModelClick = { actions.onUnpinModel(model.id, ModelType.CHAT) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (selectedTab == SelectorTab.ImageModels) {
                items(items = pinnedImageModels, key = { it.id }) { model ->
                    ModelSelectorItem(
                        modelName = model.name,
                        onUseModelClick = {
                            actions.onDismiss()
                            actions.onUseModel(ActiveModelRef.Image(model.id))
                        },
                        onUnpinModelClick = { actions.onUnpinModel(model.id, ModelType.IMAGE) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (showEmptyModelListPlaceholder) {
                item(key = "EmptyModelListPlaceholder") {
                    EmptyModelSelectorItem(
                        isSearchQueryBlank = modelSearchQuery.isBlank(),
                        onOpenFullModelCatalog = {
                            actions.onDismiss()
                            actions.onNavigateToModelCatalog()
                        },
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }
}
