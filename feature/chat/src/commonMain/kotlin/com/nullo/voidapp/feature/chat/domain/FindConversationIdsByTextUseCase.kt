package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository

/** Use case responsible for finding a conversations by query text. */
internal class FindConversationIdsByTextUseCase(private val conversationRepository: ConversationRepository) {

    suspend operator fun invoke(query: String): Set<Long> = conversationRepository.findConversationIdsByText(query)
}
