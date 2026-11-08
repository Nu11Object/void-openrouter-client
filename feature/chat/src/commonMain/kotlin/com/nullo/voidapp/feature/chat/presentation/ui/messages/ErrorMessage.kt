package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.Close
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.Message
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.dismiss
import voidapp.feature.chat.generated.resources.error_details
import voidapp.feature.chat.generated.resources.error_title_credits
import voidapp.feature.chat.generated.resources.error_title_network
import voidapp.feature.chat.generated.resources.error_title_rate_limit
import voidapp.feature.chat.generated.resources.error_title_rejected
import voidapp.feature.chat.generated.resources.error_title_server
import voidapp.feature.chat.generated.resources.error_title_unauthorized
import voidapp.feature.chat.generated.resources.error_title_unexpected
import voidapp.feature.chat.generated.resources.retry

@Composable
internal fun ErrorMessageItem(
    error: Message.Error,
    isLastMessage: Boolean,
    onRegenerateLastResponseClick: () -> Unit,
    onDismissChatError: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorTitle = stringResource(
        when (error.reason) {
            ErrorReason.NETWORK -> Res.string.error_title_network
            ErrorReason.SERVER_INTERNAL -> Res.string.error_title_server
            ErrorReason.REQUEST_REJECTED -> Res.string.error_title_rejected
            ErrorReason.INSUFFICIENT_CREDITS -> Res.string.error_title_credits
            ErrorReason.RATE_LIMITED -> Res.string.error_title_rate_limit
            ErrorReason.UNAUTHORIZED -> Res.string.error_title_unauthorized
            ErrorReason.UNEXPECTED -> Res.string.error_title_unexpected
        }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.large
            )
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = errorTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        error.rawMessage?.let { rawMessage ->
            Text(
                text = stringResource(Res.string.error_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    text = rawMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                )
            }
        }
        Row {
            AnimatedVisibility(
                visible = isLastMessage,
                label = "RegenerateResponseTextButton"
            ) {
                TextButton(
                    onClick = onRegenerateLastResponseClick,
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp).pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Restart,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(Res.string.retry))
                }
            }
            TextButton(
                onClick = { onDismissChatError(error.id) },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(Res.string.dismiss))
            }
        }
    }
}
