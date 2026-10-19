package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository

/** Use case responsible for renaming a conversation. */
internal class RenameConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {

    suspend operator fun invoke(conversationId: Long, newTitle: String) {
        conversationRepository.rename(conversationId, newTitle)
    }
}
