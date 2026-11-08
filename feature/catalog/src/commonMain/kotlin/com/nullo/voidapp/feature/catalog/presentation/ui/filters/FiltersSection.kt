package com.nullo.voidapp.feature.catalog.presentation.ui.filters

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.Chat
import com.nullo.voidapp.core.designsystem.icon.default.Check
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.core.designsystem.icon.default.Lightbulb
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.feature.catalog.domain.ChatModelFilters
import com.nullo.voidapp.feature.catalog.domain.ImageModelFilters
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State.CatalogTab
import com.nullo.voidapp.feature.catalog.presentation.ui.toCompactString
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.any
import voidapp.feature.catalog.generated.resources.context_window
import voidapp.feature.catalog.generated.resources.features
import voidapp.feature.catalog.generated.resources.filters
import voidapp.feature.catalog.generated.resources.image
import voidapp.feature.catalog.generated.resources.input_modalities
import voidapp.feature.catalog.generated.resources.input_price
import voidapp.feature.catalog.generated.resources.output_modalities
import voidapp.feature.catalog.generated.resources.output_price
import voidapp.feature.catalog.generated.resources.pinned_only
import voidapp.feature.catalog.generated.resources.price_description
import voidapp.feature.catalog.generated.resources.reasoning
import voidapp.feature.catalog.generated.resources.reset
import voidapp.feature.catalog.generated.resources.text
import voidapp.feature.catalog.generated.resources.zero_cost

@Immutable
internal data class FilterActions(
    val onChatFiltersChange: (ChatModelFilters) -> Unit = {},
    val onImageFiltersChange: (ImageModelFilters) -> Unit = {},
    val onReset: () -> Unit = {},
)

@Composable
internal fun FiltersSection(
    selectedTab: CatalogTab,
    chatModelFilters: ChatModelFilters,
    imageModelFilters: ImageModelFilters,
    activeFiltersCount: Int,
    actions: FilterActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.filters),
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedVisibility(
                visible = activeFiltersCount > 0,
                label = "ActiveFiltersCount"
            ) {
                Text(
                    text = activeFiltersCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .padding(horizontal = 8.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = actions.onReset,
                enabled = activeFiltersCount > 0,
                modifier = Modifier
                    .height(32.dp)
                    .pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Restart,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(Res.string.reset))
            }
        }
        when (selectedTab) {
            CatalogTab.ChatModels -> {
                FilterSwitch(
                    text = stringResource(Res.string.pinned_only),
                    checked = chatModelFilters.pinnedOnly,
                    onCheckedChange = {
                        actions.onChatFiltersChange(chatModelFilters.copy(pinnedOnly = it))
                    }
                )
                FilterSwitch(
                    text = stringResource(Res.string.zero_cost),
                    checked = chatModelFilters.freeOnly,
                    onCheckedChange = {
                        actions.onChatFiltersChange(chatModelFilters.copy(freeOnly = it))
                    }
                )
                AnimatedContent(
                    targetState = !chatModelFilters.freeOnly,
                    transitionSpec = { fadeIn() togetherWith fadeOut() + slideOutHorizontally() },
                    label = "FilterPriceRanges",
                    modifier = Modifier.fillMaxWidth()
                ) { visible ->
                    if (visible) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FilterItemArea(
                                title = stringResource(Res.string.input_price),
                                description = stringResource(Res.string.price_description)
                            ) {
                                PriceRangeFields(
                                    range = chatModelFilters.inputPrice,
                                    onRangeChange = {
                                        actions.onChatFiltersChange(chatModelFilters.copy(inputPrice = it))
                                    }
                                )
                            }
                            FilterItemArea(
                                title = stringResource(Res.string.output_price),
                                description = stringResource(Res.string.price_description)
                            ) {
                                PriceRangeFields(
                                    range = chatModelFilters.outputPrice,
                                    onRangeChange = {
                                        actions.onChatFiltersChange(chatModelFilters.copy(outputPrice = it))
                                    }
                                )
                            }
                        }
                    }
                }
                FilterItemArea(title = stringResource(Res.string.context_window)) {
                    NumberField(
                        value = chatModelFilters.minContextLength,
                        onValueChange = {
                            actions.onChatFiltersChange(chatModelFilters.copy(minContextLength = it))
                        },
                        parse = String::toIntOrNull,
                        format = { it.toString() },
                        placeholder = stringResource(Res.string.any),
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(128_000, 256_000, 512_000, 1_000_000).forEach { size ->
                            val selected = chatModelFilters.minContextLength == size
                            FilterTag(
                                text = size.toCompactString(),
                                selected = selected,
                                onClick = {
                                    actions.onChatFiltersChange(
                                        chatModelFilters.copy(minContextLength = if (selected) null else size)
                                    )
                                }
                            )
                        }
                    }
                }
                FeaturesFilter(
                    selected = chatModelFilters.requiredFeatures,
                    onSelectedChange = {
                        actions.onChatFiltersChange(chatModelFilters.copy(requiredFeatures = it))
                    }
                )
                ModalitiesFilter(
                    title = stringResource(Res.string.input_modalities),
                    selected = chatModelFilters.requiredInputModalities,
                    onSelectedChange = {
                        actions.onChatFiltersChange(chatModelFilters.copy(requiredInputModalities = it))
                    }
                )
                ModalitiesFilter(
                    title = stringResource(Res.string.output_modalities),
                    selected = chatModelFilters.requiredOutputModalities,
                    onSelectedChange = {
                        actions.onChatFiltersChange(chatModelFilters.copy(requiredOutputModalities = it))
                    }
                )
            }

            CatalogTab.ImageModels -> {
                FilterSwitch(
                    text = stringResource(Res.string.pinned_only),
                    checked = imageModelFilters.pinnedOnly,
                    onCheckedChange = {
                        actions.onImageFiltersChange(imageModelFilters.copy(pinnedOnly = it))
                    }
                )
                ModalitiesFilter(
                    title = stringResource(Res.string.input_modalities),
                    selected = imageModelFilters.requiredInputModalities,
                    onSelectedChange = {
                        actions.onImageFiltersChange(imageModelFilters.copy(requiredInputModalities = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterItemArea(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun FeaturesFilter(
    selected: Set<ModelFeature>,
    onSelectedChange: (Set<ModelFeature>) -> Unit,
) {
    FilterItemArea(title = stringResource(Res.string.features)) {
        TagSelector(
            options = ModelFeature.entries,
            selected = selected,
            onSelectedChange = onSelectedChange,
            label = { feature ->
                when (feature) {
                    ModelFeature.THINKING -> stringResource(Res.string.reasoning)
                }
            },
            icon = { feature ->
                when (feature) {
                    ModelFeature.THINKING -> Icons.Default.Lightbulb
                }
            }
        )
    }
}

@Composable
private fun ModalitiesFilter(
    title: String,
    selected: Set<Modality>,
    onSelectedChange: (Set<Modality>) -> Unit,
) {
    FilterItemArea(title = title) {
        TagSelector(
            options = Modality.entries,
            selected = selected,
            onSelectedChange = onSelectedChange,
            label = { modality ->
                when (modality) {
                    Modality.TEXT -> stringResource(Res.string.text)
                    Modality.IMAGE -> stringResource(Res.string.image)
                }
            },
            icon = { modality ->
                when (modality) {
                    Modality.TEXT -> Icons.Default.Chat
                    Modality.IMAGE -> Icons.Default.Image
                }
            }
        )
    }
}

@Composable
private fun FilterSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChange(!checked) },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary)
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .background(
                color = if (checked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            ),
            thumbContent = {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
    }
}
