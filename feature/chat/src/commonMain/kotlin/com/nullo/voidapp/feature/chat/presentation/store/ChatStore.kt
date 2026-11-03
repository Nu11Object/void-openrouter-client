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
        val supportsImageInput: Boolean = false,

        val messages: List<Message> = emptyList(),

        val activeModelName: String? = null,
        val modelType: ModelType? = null,

        val thinkingEnabled: Boolean = false,
        val webSearchEnabled: Boolean = false,

        val isTuningPanelOpen: Boolean = false,
        val errorDialogText: UiText? = null,
    ) {
        val isGenerating: Boolean
            get() = (messages.lastOrNull() as? Message.Completion)?.isStreaming ?: false

        val canAttachImages: Boolean
            get() = supportsImageInput &&
                !isImportingAttachments &&
                promptImages.size < MAX_ATTACHMENTS

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
