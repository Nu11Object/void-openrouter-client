package com.nullo.voidapp.feature.chat.presentation.ui.tune

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.component.VoidTextField
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Close
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_clear_value
import voidapp.feature.chat.generated.resources.tune_placeholder_default

@Composable
internal fun TuneBlock(
    texts: TuneParamTexts,
    hintsVisible: Boolean,
    modifier: Modifier = Modifier,
    supportText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = texts.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(texts.title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    AnimatedContent(targetState = supportText, label = "SupportText") {
                        it?.let { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            content()
            AnimatedVisibility(visible = hintsVisible, label = "TuneHint") {
                Text(
                    text = stringResource(texts.description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun TuneNumberField(
    text: String,
    onTextChange: (String) -> Unit,
    isInteger: Boolean,
    allowNegative: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    showClear: Boolean = false,
) {
    VoidTextField(
        value = text,
        onValueChange = { input ->
            onTextChange(
                input.filter { c ->
                    c in '0'..'9' ||
                        (!isInteger && (c == '.' || c == ',')) ||
                        (allowNegative && c == '-')
                }
            )
        },
        modifier = modifier.height(48.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = stringResource(Res.string.tune_placeholder_default),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingIcon = if (showClear && text.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onTextChange("") },
                    modifier = Modifier
                        .size(32.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.desc_clear_value),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal),
        containerColor = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    )
}

@Composable
internal fun TuneSliderControl(
    position: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    text: String,
    isInteger: Boolean,
    allowNegative: Boolean,
    errorText: String?,
    onSliderChange: (Double) -> Unit,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                value = position,
                onValueChange = { onSliderChange(it.toDouble()) },
                valueRange = valueRange,
                modifier = Modifier
                    .weight(1f)
                    .pointerHoverIcon(PointerIcon.Hand)
            )
            TuneNumberField(
                text = text,
                onTextChange = onTextChange,
                isInteger = isInteger,
                allowNegative = allowNegative,
                isError = errorText != null,
                modifier = Modifier.width(96.dp)
            )
        }
        AnimatedContent(
            targetState = errorText,
            label = "TuneSupportingLine"
        ) { error ->
            error?.let { TuneSupportingLine(text = it, isError = true) }
        }
    }
}

@Composable
internal fun TuneFieldControl(
    text: String,
    isInteger: Boolean,
    allowNegative: Boolean,
    errorText: String?,
    rangeText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TuneNumberField(
                text = text,
                onTextChange = onTextChange,
                isInteger = isInteger,
                allowNegative = allowNegative,
                isError = errorText != null,
                showClear = true,
                modifier = Modifier.weight(1f)
            )
            trailing()
        }
        TuneSupportingLine(text = errorText ?: rangeText, isError = errorText != null)
    }
}

@Composable
internal fun TuneActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp).pointerHoverIcon(PointerIcon.Hand),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.ExtraSmallIconSpacing))
        }
        Text(text = text)
    }
}

@Composable
private fun TuneSupportingLine(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        },
        modifier = Modifier.padding(start = 8.dp, top = 6.dp)
    )
}
