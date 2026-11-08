package com.nullo.voidapp.feature.chat.presentation.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.BrokenImage
import com.nullo.voidapp.core.designsystem.icon.default.Close
import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_remove_attachment

@Composable
internal fun ImageAttachmentItem(
    imageAttachment: ImageAttachment,
    modifier: Modifier = Modifier,
    onRemoveClick: (() -> Unit)? = null,
) {
    val file = remember(imageAttachment.relativePath) {
        FileKit.filesDir / imageAttachment.relativePath
    }
    Box(modifier = modifier) {
        AsyncImage(
            model = file,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = rememberVectorPainter(Icons.Default.BrokenImage),
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.small)
        )
        if (onRemoveClick != null) {
            IconButton(
                onClick = onRemoveClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.desc_remove_attachment),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
    }
}
