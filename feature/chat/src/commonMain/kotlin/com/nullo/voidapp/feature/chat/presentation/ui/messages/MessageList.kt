package com.nullo.voidapp.feature.chat.presentation.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.utils.compose.StickToBottomEffect
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher

@Immutable
internal data class MessageActions(
    val onRegenerateLastResponse: () -> Unit = {},
    val onSaveImage: (ImageAttachment, PlatformFile) -> Unit = { _, _ -> },
    val onDismissChatError: (id: Long) -> Unit = {},
)

@Composable
internal fun MessageList(
    messages: List<Message>,
    messagesTopPadding: Dp,
    actions: MessageActions,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    listState.StickToBottomEffect(resetKey = messages.lastOrNull()?.id)

    var imageToSave by remember { mutableStateOf<ImageAttachment?>(null) }
    val imageSaver = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { destination: PlatformFile? ->
        val image = imageToSave
        imageToSave = null
        if (destination != null && image != null) actions.onSaveImage(image, destination)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = messagesTopPadding,
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        )
    ) {
        itemsIndexed(
            items = messages,
            key = { _, item -> item.id },
            contentType = { _, item -> item::class }
        ) { index, message ->
            when (message) {
                is Message.Completion -> CompletionMessageItem(
                    completion = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateResponse = actions.onRegenerateLastResponse,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Error -> ErrorMessageItem(
                    error = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateLastResponseClick = actions.onRegenerateLastResponse,
                    onDismissChatError = actions.onDismissChatError,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.ImageLoading -> ImageLoadingMessageItem(
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Images -> ImagesMessageItem(
                    message = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateResponse = actions.onRegenerateLastResponse,
                    onSaveImageClick = { image ->
                        imageToSave = image
                        imageSaver.launch(
                            suggestedName = "void-generated-image",
                            defaultExtension = image.relativePath.substringAfterLast('.')
                        )
                    },
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Interrupted -> InterruptedMessageItem(
                    message = message,
                    isLastMessage = index == messages.lastIndex,
                    onRegenerateResponse = actions.onRegenerateLastResponse,
                    modifier = Modifier.animateItem(placementSpec = null)
                )

                is Message.Prompt -> PromptMessageItem(
                    prompt = message,
                    modifier = Modifier.animateItem(placementSpec = null)
                )
            }
        }
    }
}
