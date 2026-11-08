package com.nullo.voidapp.feature.catalog.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Chat
import com.nullo.voidapp.core.designsystem.icon.default.Check
import com.nullo.voidapp.core.designsystem.icon.default.CloudSync
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State.CatalogTab
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.chat_models
import voidapp.feature.catalog.generated.resources.image_models
import voidapp.feature.catalog.generated.resources.refresh
import voidapp.feature.catalog.generated.resources.syncing
import voidapp.feature.catalog.generated.resources.verbose

@Composable
internal fun CatalogFloatingToolbar(
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
internal fun CatalogTabSwitcher(
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
internal fun ControlsSection(
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
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.verbose),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
