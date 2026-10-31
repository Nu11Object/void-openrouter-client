package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.MessageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.database.entity.MessageWithImages
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message

fun MessageEntity.toDomain(): Message = when (this.messageType) {
    MessageType.PROMPT -> Message.Prompt(
        id = id,
        query = text.orEmpty()
    )

    MessageType.COMPLETION -> Message.Completion(
        id = id,
        text = text.orEmpty(),
        reasoning = reasoning,
        isStreaming = isGenerating
    )

    MessageType.IMAGES -> Message.Images(
        id = id,
        // todo: captions & images
        images = emptyList()
    )

    MessageType.ERROR -> Message.Error(
        id = id,
        reason = ErrorReason.fromKey(errorReason),
        rawMessage = errorRawMessage
    )

    MessageType.INTERRUPTED -> Message.Interrupted(
        id = id,
        text = text
    )
}

fun MessageWithImages.toPrompt(): Message.Prompt = Message.Prompt(
    id = message.id,
    query = message.text.orEmpty(),
    images = images
        .sortedBy { it.position }
        .map { ImageAttachment(it.relativePath, it.mimeType) }
)

fun MessageWithImages.toDomain(): Message = when (message.messageType) {
    MessageType.PROMPT -> toPrompt()
    else -> message.toDomain()
}
