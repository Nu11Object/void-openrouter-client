package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.model.ActiveModelRef

/** Use case responsible for regenerating the response to the last prompt. */
internal class RegenerateLastResponseUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(conversationId: Long, activeModelRef: ActiveModelRef) {
        chatRepository.regenerateLastResponse(conversationId, activeModelRef)
    }
}
