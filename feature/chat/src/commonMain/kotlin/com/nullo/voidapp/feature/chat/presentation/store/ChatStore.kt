package com.nullo.voidapp.feature.chat.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.utils.resources.UiText
import io.github.vinceglb.filekit.PlatformFile

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, Nothing> {

    @Immutable
    data class State(
        val prompt: String = "",

        val promptImages: List<ImageAttachment> = emptyList(),
        val isImportingAttachments: Boolean = false,
        val maxAttachments: Int = 0,

        val messages: List<Message> = emptyList(),

        val activeModelName: String? = null,
        val modelType: ModelType? = null,

        val thinkingEnabled: Boolean = false,
        val webSearchEnabled: Boolean = false,

        val isTuningPanelOpen: Boolean = false,
        val errorDialogText: UiText? = null,
    ) {
        val isGenerating: Boolean
            get() = when (val last = messages.lastOrNull()) {
                is Message.Completion -> last.isStreaming
                is Message.ImageLoading -> true
                else -> false
            }

        val supportsImageInput: Boolean
            get() = maxAttachments > 0

        val canAttachImages: Boolean
            get() = !isImportingAttachments && promptImages.size < maxAttachments

        val canSendPrompt: Boolean
            get() = !isImportingAttachments && (
                prompt.isNotBlank() ||
                    (promptImages.isNotEmpty() && modelType != ModelType.IMAGE)
                )

        companion object {

            const val MAX_ATTACHMENTS = 8
        }
    }

    sealed interface Intent {

        data object OpenTuningPanel : Intent
        data object CloseTuningPanel : Intent

        data class AttachImages(val files: List<PlatformFile>) : Intent
        data class RemoveImageAttachment(val attachment: ImageAttachment) : Intent
        data class SaveImage(
            val image: ImageAttachment,
            val destination: PlatformFile,
        ) : Intent

        data object ToggleThinking : Intent
        data object ToggleWebSearch : Intent

        data class ChangePrompt(val prompt: String) : Intent
        data object SendPrompt : Intent
        data object RegenerateLastResponse : Intent
        data object StopGeneration : Intent

        data class DismissChatError(val messageId: Long) : Intent
        data object DismissErrorDialog : Intent
    }
}
