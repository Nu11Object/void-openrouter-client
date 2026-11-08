package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime.Companion.Format
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.loading_generation
import voidapp.feature.chat.generated.resources.template_by_model

@Composable
internal fun MessageActionButton(
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
internal fun ResponseInfo(
    createdAt: Long?,
    modelId: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        createdAt?.let { timestamp ->
            Text(
                text = timestamp.toFormattedDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        modelId?.let { model ->
            Text(
                text = stringResource(Res.string.template_by_model, model),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun GeneratingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
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
}

private val dateTimeFormat = Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
    chars(" · ")
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

internal fun Long.toFormattedDateTime(): String =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(dateTimeFormat)
