package com.nullo.voidapp.feature.catalog.presentation.ui.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.designsystem.component.VoidTextField
import com.nullo.voidapp.feature.catalog.domain.PriceRange
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.price_from
import voidapp.feature.catalog.generated.resources.price_to

@Composable
internal fun <T : Comparable<T>> NumberField(
    value: T?,
    onValueChange: (T?) -> Unit,
    parse: (String) -> T?,
    format: (T) -> String,
    placeholder: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
) {
    var text by remember { mutableStateOf(value?.let(format).orEmpty()) }

    // External changes (Reset, quick values) update the text.
    LaunchedEffect(value) {
        val typed = parse(text)
        val same = if (typed == null || value == null) typed == value else typed.compareTo(value) == 0
        if (!same) text = value?.let(format).orEmpty()
    }

    VoidTextField(
        value = text,
        onValueChange = { input ->
            text = input.replace(',', '.').filter { it.isDigit() || (decimal && it == '.') }
            val parsed = parse(text)
            if (text.isBlank() || parsed != null) onValueChange(parsed)
        },
        modifier = modifier.height(48.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
internal fun PriceRangeFields(
    range: PriceRange,
    onRangeChange: (PriceRange) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberField(
            value = range.min,
            onValueChange = { onRangeChange(range.copy(min = it)) },
            parse = String::toPriceOrNull,
            format = { it.toPlainString() },
            placeholder = stringResource(Res.string.price_from),
            decimal = true,
            modifier = Modifier.weight(1f)
        )
        NumberField(
            value = range.max,
            onValueChange = { onRangeChange(range.copy(max = it)) },
            parse = String::toPriceOrNull,
            format = { it.toPlainString() },
            placeholder = stringResource(Res.string.price_to),
            decimal = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun FilterTag(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .toggleable(
                value = selected,
                onValueChange = { onClick() },
                role = Role.Checkbox
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
internal fun <T> TagSelector(
    options: List<T>,
    selected: Set<T>,
    onSelectedChange: (Set<T>) -> Unit,
    label: @Composable (T) -> String,
    icon: (T) -> ImageVector? = { null },
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            FilterTag(
                text = label(option),
                icon = icon(option),
                selected = option in selected,
                onClick = {
                    onSelectedChange(if (option in selected) selected - option else selected + option)
                }
            )
        }
    }
}

private fun String.toPriceOrNull(): BigDecimal? = runCatching {
    trimEnd('.').let { if (it.startsWith('.')) "0$it" else it }.toBigDecimal()
}.getOrNull()
