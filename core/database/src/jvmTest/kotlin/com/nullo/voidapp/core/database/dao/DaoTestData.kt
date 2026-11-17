package com.nullo.voidapp.core.database.dao

import com.nullo.voidapp.core.database.AppDatabase
import com.nullo.voidapp.core.database.entity.ConversationEntity
import com.nullo.voidapp.core.database.entity.MessageEntity
import com.nullo.voidapp.core.database.entity.MessageImageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.database.entity.MessageWithImages
import kotlinx.coroutines.flow.first

internal fun conversationEntity(
    title: String? = "Conversation",
    createdAt: Long = 1L,
    updatedAt: Long = 1L,
) = ConversationEntity(
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun messageEntity(
    conversationId: Long,
    messageType: MessageType = MessageType.PROMPT,
    promptId: Long? = null,
    text: String? = null,
    reasoning: String? = null,
    isGenerating: Boolean = false,
    errorReason: String? = null,
    errorRawMessage: String? = null,
    modelId: String? = null,
    createdAt: Long? = null,
    searchText: String? = null,
) = MessageEntity(
    conversationId = conversationId,
    messageType = messageType,
    promptId = promptId,
    text = text,
    reasoning = reasoning,
    isGenerating = isGenerating,
    errorReason = errorReason,
    errorRawMessage = errorRawMessage,
    modelId = modelId,
    createdAt = createdAt,
    searchText = searchText
)

internal fun messageImageEntity(
    messageId: Long,
    relativePath: String,
    mimeType: String = "image/png",
    position: Int = 0,
) = MessageImageEntity(
    messageId = messageId,
    relativePath = relativePath,
    mimeType = mimeType,
    position = position
)

internal suspend fun AppDatabase.newConversation(): Long =
    conversationDao().insert(conversationEntity())

internal suspend fun AppDatabase.messagesOf(conversationId: Long): List<MessageWithImages> =
    messageDao().observeByConversation(conversationId).first()

internal suspend fun AppDatabase.storedMessage(conversationId: Long, messageId: Long): MessageEntity =
    messagesOf(conversationId).single { it.message.id == messageId }.message
