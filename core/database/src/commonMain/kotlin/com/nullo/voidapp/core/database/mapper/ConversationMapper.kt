package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.ConversationEntity
import com.nullo.voidapp.core.model.Conversation

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)
