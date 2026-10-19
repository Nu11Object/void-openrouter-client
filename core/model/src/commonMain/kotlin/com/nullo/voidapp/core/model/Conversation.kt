package com.nullo.voidapp.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Conversation(
    val id: Long,
    val title: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
