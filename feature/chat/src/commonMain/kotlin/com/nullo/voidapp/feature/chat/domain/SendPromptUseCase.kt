package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.model.ActiveModelRef

/** Use case responsible for sending prompt and persisting the response. */
internal class SendPromptUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        conversationId: Long,
        prompt: String,
        activeModelRef: ActiveModelRef,
    ) {
        chatRepository.sendPrompt(conversationId, prompt, activeModelRef)
    }
}
