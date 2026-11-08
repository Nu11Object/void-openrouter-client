package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ChevronRight
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.ContentCopy
import com.nullo.voidapp.core.designsystem.theme.chatMarkdownTypography
import com.nullo.voidapp.core.model.Message
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_copy_last_response
import voidapp.feature.chat.generated.resources.desc_regenerate_last_response
import voidapp.feature.chat.generated.resources.hide_reasoning
import voidapp.feature.chat.generated.resources.nothing_was_generated
import voidapp.feature.chat.generated.resources.show_reasoning
import voidapp.feature.chat.generated.resources.textbutton_retry_interupted_response

@Composable
internal fun CompletionMessageItem(
    completion: Message.Completion,
    isLastMessage: Boolean,
    onRegenerateResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val layoutDirection = LocalLayoutDirection.current

    val reasoningMdState = rememberMarkdownState(completion.reasoning.orEmpty(), retainState = true)
    val completionMdState = rememberMarkdownState(completion.text, retainState = true)

    var reasoningExpanded by rememberSaveable(completion.id) { mutableStateOf(false) }
    val reasoningChevronRotation = animateFloatAsState(
        targetValue = if (reasoningExpanded) {
            if (layoutDirection == LayoutDirection.Rtl) -90f else 90f
        } else {
            0f
        }
    )

    Column(modifier = modifier) {
        if (completion.reasoning != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        onClick = { reasoningExpanded = !reasoningExpanded },
                        indication = null,
                        interactionSource = null,
                        role = Role.Button
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    text = stringResource(
                        if (reasoningExpanded) {
                            Res.string.hide_reasoning
                        } else {
                            Res.string.show_reasoning
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.animateContentSize(alignment = Alignment.CenterEnd)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = reasoningChevronRotation.value
                    }
                )
            }
            AnimatedVisibility(
                visible = reasoningExpanded,
                label = "ReasoningText"
            ) {
                SelectionContainer {
                    Markdown(
                        markdownState = reasoningMdState,
                        typography = chatMarkdownTypography(),
                        imageTransformer = Coil3ImageTransformerImpl,
                        components = rememberMarkdownComponents(),
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (completion.text.isNotEmpty()) {
            SelectionContainer {
                Markdown(
                    markdownState = completionMdState,
                    typography = chatMarkdownTypography(),
                    imageTransformer = Coil3ImageTransformerImpl,
                    components = rememberMarkdownComponents()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = completion.isStreaming,
            label = "CompletionMessageItemContent"
        ) { isStreaming ->
            if (isStreaming) {
                GeneratingIndicator()
            } else {
                Column {
                    ResponseInfo(completion.createdAt, completion.modelId)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x = (-8).dp)
                    ) {
                        if (isLastMessage) {
                            MessageActionButton(
                                icon = Icons.AutoMirrored.Restart,
                                onClick = onRegenerateResponse,
                                contentDescription = stringResource(
                                    Res.string.desc_regenerate_last_response
                                )
                            )
                        }
                        MessageActionButton(
                            icon = Icons.Default.ContentCopy,
                            onClick = { clipboardManager.setText(AnnotatedString(completion.text)) },
                            contentDescription = stringResource(Res.string.desc_copy_last_response)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun InterruptedMessageItem(
    message: Message.Interrupted,
    isLastMessage: Boolean,
    onRegenerateResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectionContainer {
            Markdown(
                content = message.text ?: stringResource(Res.string.nothing_was_generated),
                typography = chatMarkdownTypography(),
                imageTransformer = Coil3ImageTransformerImpl,
                components = rememberMarkdownComponents()
            )
        }
        AnimatedVisibility(
            visible = isLastMessage,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            label = "RetryInterrupted"
        ) {
            TextButton(
                onClick = onRegenerateResponse,
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(Res.string.textbutton_retry_interupted_response),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun rememberMarkdownComponents() = remember {
    markdownComponents(
        codeBlock = {
            MarkdownHighlightedCodeBlock(
                content = it.content,
                node = it.node,
                showHeader = true
            )
        },
        codeFence = {
            MarkdownHighlightedCodeFence(
                content = it.content,
                node = it.node,
                showHeader = true
            )
        }
    )
}
