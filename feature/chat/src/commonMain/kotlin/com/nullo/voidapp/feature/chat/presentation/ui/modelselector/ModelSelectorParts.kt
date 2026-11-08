package com.nullo.voidapp.feature.chat.presentation.ui.modelselector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ChevronRight
import com.nullo.voidapp.core.designsystem.icon.default.Chat
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.core.designsystem.icon.default.KeepOff
import com.nullo.voidapp.core.designsystem.icon.default.SearchOff
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.State.SelectorTab
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.chat_models
import voidapp.feature.chat.generated.resources.image_models
import voidapp.feature.chat.generated.resources.no_models_found
import voidapp.feature.chat.generated.resources.no_pinned_models
import voidapp.feature.chat.generated.resources.open_model_catalog
import voidapp.feature.chat.generated.resources.template_unpin_model

@Composable
internal fun ModelSelectorItem(
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
internal fun EmptyModelSelectorItem(
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
internal fun ModelSelectorTabSwitcher(
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
