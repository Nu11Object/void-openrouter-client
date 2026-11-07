package com.nullo.voidapp.feature.chat.presentation.ui.tune

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Shuffle
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.feature.chat.domain.tune.ChatTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.ImageTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.TuneInput
import com.nullo.voidapp.feature.chat.domain.tune.TuneParamSpec
import com.nullo.voidapp.feature.chat.domain.tune.supportFor
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.template_tune_range
import voidapp.feature.chat.generated.resources.tune_action_random
import voidapp.feature.chat.generated.resources.tune_action_set_max

@Composable
internal fun ChatTuneBlock(
    item: ChatTuneItem,
    hintsVisible: Boolean,
    maxTokensLimit: Int?,
    actions: TuneActions,
    modifier: Modifier = Modifier,
) {
    val spec = item.spec
    val parameter = spec.parameter
    val texts = remember(parameter) { parameter.tuneTexts() }
    val errorText = (item.input as? TuneInput.Invalid)
        ?.message(min = spec.format(spec.min), max = spec.format(spec.max))
    val supportText = spec.effectiveValue(item.input)
        ?.let { parameter.supportFor(it) }
        ?.let { stringResource(it.textRes()) }

    TuneBlock(
        texts = texts,
        hintsVisible = hintsVisible,
        modifier = modifier,
        supportText = supportText
    ) {
        when (spec.control) {
            TuneParamSpec.Control.SLIDER -> TuneSliderControl(
                position = ((item.input as? TuneInput.Valid)?.value ?: spec.defaultValue ?: spec.min)
                    .coerceIn(spec.min, spec.max)
                    .toFloat(),
                valueRange = spec.min.toFloat()..spec.max.toFloat(),
                text = item.displayText,
                isInteger = spec.isInteger,
                allowNegative = spec.min < 0,
                errorText = errorText,
                onSliderChange = { actions.onChatSliderChange(parameter, it) },
                onTextChange = { actions.onChatValueChange(parameter, it) }
            )

            TuneParamSpec.Control.FIELD -> TuneFieldControl(
                text = item.displayText,
                isInteger = spec.isInteger,
                allowNegative = spec.min < 0,
                errorText = errorText,
                rangeText = stringResource(
                    Res.string.template_tune_range,
                    spec.format(spec.min),
                    spec.format(spec.max)
                ),
                onTextChange = { actions.onChatValueChange(parameter, it) },
                trailing = {
                    if (parameter == TuningParameter.SEED) {
                        TuneActionButton(
                            text = stringResource(Res.string.tune_action_random),
                            icon = Icons.Default.Shuffle,
                            onClick = actions.onChatSeedRandomize
                        )
                    }
                    if (parameter == TuningParameter.MAX_TOKENS && maxTokensLimit != null) {
                        TuneActionButton(
                            text = stringResource(Res.string.tune_action_set_max),
                            onClick = actions.onMaxTokensSetToLimit
                        )
                    }
                }
            )
        }
    }
}

@Composable
internal fun ImageTuneBlock(
    item: ImageTuneItem,
    hintsVisible: Boolean,
    actions: TuneActions,
    modifier: Modifier = Modifier,
) {
    val texts = remember(item.parameter) { item.parameter.tuneTexts() } ?: return

    TuneBlock(texts = texts, hintsVisible = hintsVisible, modifier = modifier) {
        when (item) {
            is ImageTuneItem.Choice -> FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.options.forEach { option ->
                    TuneChip(
                        label = option,
                        selected = option == item.selected,
                        onClick = { actions.onImageOptionSelect(item.parameter, option) }
                    )
                }
            }

            is ImageTuneItem.Numeric -> ImageNumericControl(item, actions)
        }
    }
}

@Composable
private fun ImageNumericControl(
    item: ImageTuneItem.Numeric,
    actions: TuneActions,
) {
    val spec = item.spec
    val errorText = (item.input as? TuneInput.Invalid)?.message(spec.format(spec.min), spec.format(spec.max))
    val text = item.raw.orEmpty()

    if (spec.hasSlider && spec.max > spec.min) {
        TuneSliderControl(
            position = ((item.input as? TuneInput.Valid)?.value ?: spec.min).toFloat(),
            valueRange = spec.min.toFloat()..spec.max.toFloat(),
            text = text,
            isInteger = spec.isInteger,
            allowNegative = spec.min < 0,
            errorText = errorText,
            onSliderChange = { actions.onImageSliderChange(item.parameter, it) },
            onTextChange = { actions.onImageValueChange(item.parameter, it) }
        )
    } else {
        TuneFieldControl(
            text = text,
            isInteger = spec.isInteger,
            allowNegative = spec.min < 0,
            errorText = errorText,
            rangeText = stringResource(
                Res.string.template_tune_range,
                spec.format(spec.min),
                spec.format(spec.max)
            ),
            onTextChange = { actions.onImageValueChange(item.parameter, it) },
            trailing = {
                if (item.parameter == ImageParameter.SEED) {
                    TuneActionButton(
                        text = stringResource(Res.string.tune_action_random),
                        icon = Icons.Default.Shuffle,
                        onClick = actions.onImageSeedRandomize
                    )
                }
            }
        )
    }
}

@Composable
private fun TuneChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        shape = CircleShape,
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    )
}
