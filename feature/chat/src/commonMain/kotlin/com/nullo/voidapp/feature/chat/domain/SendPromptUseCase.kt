package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ImageAttachment

/** Use case responsible for sending prompt and persisting the response. */
internal class SendPromptUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        conversationId: Long,
        prompt: String,
        images: List<ImageAttachment>,
        activeModelRef: ActiveModelRef,
    ) {
        chatRepository.sendPrompt(conversationId, prompt, images, activeModelRef)
    }
}
