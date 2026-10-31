package com.nullo.voidapp.core.database.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class MessageWithImages(
    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["messageId"]
    )
    val images: List<MessageImageEntity>,
)
