package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository

/** Use case responsible for creating a new conversation. Return its ID. */
internal class StartNewConversationUseCase(private val conversationRepository: ConversationRepository) {

    suspend operator fun invoke(firstQuery: String): Long = conversationRepository.createConversation(firstQuery)
}
