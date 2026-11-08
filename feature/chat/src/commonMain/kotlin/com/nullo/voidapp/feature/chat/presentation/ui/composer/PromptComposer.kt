package com.nullo.voidapp.feature.chat.presentation.ui.composer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.ArrowUpward
import com.nullo.voidapp.core.designsystem.icon.default.Attach
import com.nullo.voidapp.core.designsystem.icon.default.Explore
import com.nullo.voidapp.core.designsystem.icon.default.Lightbulb
import com.nullo.voidapp.core.designsystem.icon.default.Stop
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.utils.compose.EdgeFadeSpec
import com.nullo.voidapp.core.utils.compose.FadeDirection
import com.nullo.voidapp.core.utils.compose.edgeFades
import com.nullo.voidapp.feature.chat.presentation.ui.common.ImageAttachmentItem
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_attach_files
import voidapp.feature.chat.generated.resources.desc_send_message
import voidapp.feature.chat.generated.resources.desc_stop_generation
import voidapp.feature.chat.generated.resources.option_search
import voidapp.feature.chat.generated.resources.option_think
import voidapp.feature.chat.generated.resources.placeholder_empty_prompt

private const val PROMPT_MAX_LINES = 6

@Immutable
internal data class ComposerActions(
    val onPromptChange: (String) -> Unit = {},
    val onSendPrompt: () -> Unit = {},
    val onStopGeneration: () -> Unit = {},
    val onToggleThinking: () -> Unit = {},
    val onToggleWebSearch: () -> Unit = {},
    val onAttachImages: (List<PlatformFile>) -> Unit = {},
    val onRemoveAttachment: (ImageAttachment) -> Unit = {},
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PromptComposer(
    prompt: String,
    promptImages: List<ImageAttachment>,
    isGenerating: Boolean,
    isImportingAttachments: Boolean,
    canAttachImages: Boolean,
    canSendPrompt: Boolean,
    thinkingEnabled: Boolean,
    webSearchEnabled: Boolean,
    actions: ComposerActions,
    modifier: Modifier = Modifier,
) {
    val imagePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple()
    ) { files ->
        if (!files.isNullOrEmpty()) actions.onAttachImages(files)
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp)
    ) {
        AnimatedVisibility(
            visible = promptImages.isNotEmpty(),
            label = "AttachmentsSection"
        ) {
            AttachmentsSection(
                images = promptImages,
                onRemoveAttachmentClick = actions.onRemoveAttachment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .edgeFades(
                        EdgeFadeSpec(FadeDirection.Start, sizeFraction = 0.05f),
                        EdgeFadeSpec(FadeDirection.End, sizeFraction = 0.05f)
                    )
            )
        }

        PromptTextField(
            prompt = prompt,
            onPromptChange = actions.onPromptChange
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = isImportingAttachments,
                label = "AttachmentButton"
            ) { importing ->
                if (importing) {
                    LoadingIndicator(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(32.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    AttachmentButton(
                        enabled = canAttachImages,
                        onClick = imagePickerLauncher::launch
                    )
                }
            }
            GenerationOption(
                icon = Icons.Default.Lightbulb,
                label = stringResource(Res.string.option_think),
                enabled = thinkingEnabled,
                onClick = actions.onToggleThinking
            )
            Spacer(Modifier.width(4.dp))
            GenerationOption(
                icon = Icons.Default.Explore,
                label = stringResource(Res.string.option_search),
                enabled = webSearchEnabled,
                onClick = actions.onToggleWebSearch
            )
            Spacer(Modifier.weight(1f))
            SendPromptButton(
                isGenerating = isGenerating,
                canSendPrompt = canSendPrompt,
                onStopGeneration = actions.onStopGeneration,
                onSendPrompt = actions.onSendPrompt
            )
        }
    }
}

@Composable
private fun AttachmentsSection(
    images: List<ImageAttachment>,
    onRemoveAttachmentClick: (ImageAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(images, key = { it.relativePath }) { image ->
            ImageAttachmentItem(
                imageAttachment = image,
                onRemoveClick = { onRemoveAttachmentClick(image) },
                modifier = Modifier.size(56.dp).animateItem()
            )
        }
    }
}

@Composable
private fun SendPromptButton(
    isGenerating: Boolean,
    canSendPrompt: Boolean,
    onStopGeneration: () -> Unit,
    onSendPrompt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = if (isGenerating) onStopGeneration else onSendPrompt,
        enabled = isGenerating || canSendPrompt,
        colors = IconButtonDefaults.filledIconButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Crossfade(
            targetState = isGenerating,
            label = "SendPromptButtonContent"
        ) { generating ->
            Icon(
                imageVector = if (generating) {
                    Icons.Default.Stop
                } else {
                    Icons.Default.ArrowUpward
                },
                contentDescription = if (generating) {
                    stringResource(Res.string.desc_stop_generation)
                } else {
                    stringResource(Res.string.desc_send_message)
                }
            )
        }
    }
}

@Composable
private fun AttachmentButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = Icons.Default.Attach,
            contentDescription = stringResource(Res.string.desc_attach_files)
        )
    }
}

@Composable
private fun PromptTextField(
    prompt: String,
    onPromptChange: (String) -> Unit,
) {
    TextField(
        value = prompt,
        onValueChange = onPromptChange,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        maxLines = PROMPT_MAX_LINES,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = stringResource(Res.string.placeholder_empty_prompt),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
private fun GenerationOption(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = enabled,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = CircleShape,
        contentPadding = PaddingValues(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}
