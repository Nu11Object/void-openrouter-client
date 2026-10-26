package com.nullo.voidapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.nullo.voidapp.core.model.ErrorReason

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId"), Index("promptId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val messageType: MessageType,
    val promptId: Long? = null,
    val text: String? = null,
    val reasoning: String? = null,
    val isGenerating: Boolean = false,
    val errorReason: String? = null,
    val errorRawMessage: String? = null,
    val modelId: String? = null,
    val createdAt: Long? = null,
    val searchText: String? = null,
)

enum class MessageType {
    PROMPT,
    COMPLETION,
    IMAGES,
    ERROR,
    INTERRUPTED,
}
