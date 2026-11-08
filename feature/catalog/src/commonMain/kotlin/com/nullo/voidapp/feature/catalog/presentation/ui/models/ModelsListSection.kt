package com.nullo.voidapp.feature.catalog.presentation.ui.models

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.CloudSync
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.compose.bottomFade
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State.CatalogTab
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.auto_sync_description
import voidapp.feature.catalog.generated.resources.no_models_found

@Immutable
internal data class ModelActions(
    val onUseModel: (ActiveModelRef) -> Unit = {},
    val onPinModel: (modelId: String, ModelType, pin: Boolean) -> Unit = { _, _, _ -> },
    val onImagePriceRequest: (ImageModel) -> Unit = {},
)

@Composable
internal fun ModelsListSection(
    state: CatalogStore.State,
    actions: ModelActions,
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
                            onUseModelClick = actions.onUseModel,
                            onPinModelClick = actions.onPinModel,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                is CatalogTab.ImageModels -> {
                    items(
                        state.imageModels,
                        key = { it.id }
                    ) { model ->
                        ImageModelItem(
                            imageModel = model,
                            verbose = state.showVerboseInfo,
                            pricing = state.imagePricing[model.id],
                            onCheckPriceClick = { actions.onImagePriceRequest(model) },
                            onUseModelClick = actions.onUseModel,
                            onPinModelClick = actions.onPinModel,
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
