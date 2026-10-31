package com.nullo.voidapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "message_images",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class MessageImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val relativePath: String,
    val mimeType: String,
    val position: Int,
)
