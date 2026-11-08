package com.nullo.voidapp.feature.catalog.presentation.ui.models

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Keep
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.check_price
import voidapp.feature.catalog.generated.resources.pin
import voidapp.feature.catalog.generated.resources.unpin
import voidapp.feature.catalog.generated.resources.use_now

private val modelCardPadding = 16.dp

@Composable
internal fun ModelCard(
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
            .padding(modelCardPadding),
        content = content
    )
}

@Composable
internal fun ModelInteractionButtons(
    onUseModelClick: () -> Unit,
    onPinModelClick: () -> Unit,
    isModelPinned: Boolean,
    modifier: Modifier = Modifier,
    showCheckPriceButton: Boolean = false,
    onCheckPriceClick: (() -> Unit)? = null,
) {
    Column {
        AnimatedContent(
            targetState = showCheckPriceButton,
            label = "CheckPriceButton"
        ) {
            if (showCheckPriceButton) {
                Button(
                    onClick = { onCheckPriceClick?.invoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(text = stringResource(Res.string.check_price))
                }
            }
        }
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
}

@Composable
internal fun OutlineTag(
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
