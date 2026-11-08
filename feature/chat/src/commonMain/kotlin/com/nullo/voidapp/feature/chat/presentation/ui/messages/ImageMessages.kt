package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.BrokenImage
import com.nullo.voidapp.core.designsystem.icon.default.Download
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_generated_image
import voidapp.feature.chat.generated.resources.desc_regenerate_last_response
import voidapp.feature.chat.generated.resources.desc_save_image

private val GENERATED_IMAGE_MAX_WIDTH = 480.dp
private val GENERATED_IMAGE_MAX_HEIGHT = 560.dp

@Composable
internal fun ImageLoadingMessageItem(modifier: Modifier = Modifier) {
    GeneratingIndicator(modifier = modifier)
}

@Composable
internal fun ImagesMessageItem(
    message: Message.Images,
    isLastMessage: Boolean,
    onRegenerateResponse: () -> Unit,
    onSaveImageClick: (ImageAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            message.images.forEach { image ->
                GeneratedImageItem(
                    image = image,
                    onSaveClick = { onSaveImageClick(image) }
                )
            }
        }
        ResponseInfo(createdAt = message.createdAt, modelId = message.modelId)
        if (isLastMessage) {
            MessageActionButton(
                icon = Icons.AutoMirrored.Restart,
                onClick = onRegenerateResponse,
                contentDescription = stringResource(Res.string.desc_regenerate_last_response),
                modifier = Modifier.offset(x = (-8).dp)
            )
        }
    }
}

@Composable
internal fun GeneratedImageItem(
    image: ImageAttachment,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val file = remember(image.relativePath) { FileKit.filesDir / image.relativePath }
    var aspectRatio by remember(image.relativePath) { mutableFloatStateOf(1f) }
    var failed by remember(image.relativePath) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .widthIn(max = GENERATED_IMAGE_MAX_WIDTH)
            .heightIn(max = GENERATED_IMAGE_MAX_HEIGHT)
            .aspectRatio(aspectRatio)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = file,
            contentDescription = stringResource(Res.string.desc_generated_image),
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.isSpecified && size.width > 0f && size.height > 0f) {
                    aspectRatio = size.width / size.height
                }
            },
            onError = { failed = true },
            modifier = Modifier.fillMaxSize()
        )
        if (failed) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            IconButton(
                onClick = onSaveClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(Res.string.desc_save_image),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
