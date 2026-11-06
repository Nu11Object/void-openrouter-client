package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.MessageImageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.database.entity.MessageWithImages
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message

fun MessageWithImages.toDomain(): Message = when (message.messageType) {
    MessageType.PROMPT -> toPrompt()

    MessageType.COMPLETION -> Message.Completion(
        id = message.id,
        text = message.text.orEmpty(),
        reasoning = message.reasoning,
        isStreaming = message.isGenerating,
        modelId = message.modelId,
        createdAt = message.createdAt
    )

    MessageType.IMAGES -> if (message.isGenerating) {
        Message.ImageLoading(message.id)
    } else {
        Message.Images(
            id = message.id,
            images = attachments(),
            modelId = message.modelId,
            createdAt = message.createdAt
        )
    }

    MessageType.ERROR -> Message.Error(
        id = message.id,
        reason = ErrorReason.fromKey(message.errorReason),
        rawMessage = message.errorRawMessage
    )

    MessageType.INTERRUPTED -> Message.Interrupted(
        id = message.id,
        text = message.text
    )
}

fun MessageWithImages.toPrompt(): Message.Prompt = Message.Prompt(
    id = message.id,
    query = message.text.orEmpty(),
    images = attachments(),
    createdAt = message.createdAt
)

private fun MessageWithImages.attachments(): List<ImageAttachment> = images
    .sortedBy { it.position }
    .map { ImageAttachment(it.relativePath, it.mimeType) }

internal fun List<ImageAttachment>.toEntities(messageId: Long) = mapIndexed { index, image ->
    MessageImageEntity(
        messageId = messageId,
        relativePath = image.relativePath,
        mimeType = image.mimeType,
        position = index
    )
}
