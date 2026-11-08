package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.ContentCopy
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.feature.chat.presentation.ui.common.ImageAttachmentItem
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_copy_prompt

@Composable
internal fun PromptMessageItem(
    prompt: Message.Prompt,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(align = Alignment.End)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (prompt.images.isNotEmpty()) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prompt.images.forEach { image ->
                            ImageAttachmentItem(
                                imageAttachment = image,
                                modifier = Modifier.size(if (prompt.query.isNotBlank()) 32.dp else 64.dp)
                            )
                        }
                    }
                }
                if (prompt.query.isNotBlank()) {
                    SelectionContainer {
                        Text(
                            text = prompt.query,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            prompt.createdAt?.let { timestamp ->
                Text(
                    text = timestamp.toFormattedDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.End
                )
            }
            if (prompt.query.isNotBlank()) {
                MessageActionButton(
                    icon = Icons.Default.ContentCopy,
                    onClick = { clipboardManager.setText(AnnotatedString(prompt.query)) },
                    contentDescription = stringResource(Res.string.desc_copy_prompt)
                )
            }
        }
    }
}
