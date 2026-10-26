package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository

/** Use case responsible for deleting a conversation. */
internal class DeleteConversationUseCase(private val conversationRepository: ConversationRepository) {

    suspend operator fun invoke(conversationId: Long) {
        conversationRepository.delete(conversationId)
    }
}
