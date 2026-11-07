package com.nullo.voidapp.core.data.chat.data

import com.nullo.voidapp.core.data.chat.data.mapper.buildChatCompletionRequest
import com.nullo.voidapp.core.data.chat.data.mapper.buildImageGenerationRequest
import com.nullo.voidapp.core.data.chat.data.mapper.toCompletionMessageFlow
import com.nullo.voidapp.core.data.chat.data.mapper.toDomain
import com.nullo.voidapp.core.data.chat.data.service.ChatApiService
import com.nullo.voidapp.core.data.chat.data.service.RequestRejectedException
import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.data.chat.domain.ImageEncoder
import com.nullo.voidapp.core.data.chat.domain.ResponseGenerator
import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class ResponseGeneratorImpl(
    private val chatApiService: ChatApiService,
    private val sessionRepository: SessionRepository,
    private val imageEncoder: ImageEncoder,
    private val attachmentStorage: AttachmentStorage,
) : ResponseGenerator {

    override fun generateResponse(
        history: List<Message>,
        prompt: Message.Prompt,
        responseMessageId: Long,
        activeModel: ActiveModelRef,
    ): Flow<Message> = flow {
        when (activeModel) {
            is ActiveModelRef.Chat -> {
                val request = buildChatCompletionRequest(
                    modelId = activeModel.id,
                    prompt = prompt,
                    history = history,
                    tuningParameters = sessionRepository.chatTuningParameters.first(),
                    thinkingEnabled = sessionRepository.thinkingEnabled.first(),
                    webSearchEnabled = sessionRepository.webSearchEnabled.first(),
                    encodeImage = imageEncoder::encodeAsDataUrl
                )

                emitAll(
                    chatApiService
                        .streamChatCompletion(request)
                        .toCompletionMessageFlow(responseMessageId)
                )
            }

            is ActiveModelRef.Image -> {
                val referenceImages = prompt.images.map {
                    imageEncoder.encodeAsDataUrl(it.relativePath, it.mimeType)
                }
                val request = buildImageGenerationRequest(
                    modelId = activeModel.id,
                    prompt = prompt.query,
                    referenceImages = referenceImages,
                    parameters = sessionRepository.imageParameters.first()
                )

                val response = chatApiService.generateImage(request)
                if (response.data.isEmpty()) {
                    throw RequestRejectedException(rawMessage = NO_IMAGES_MESSAGE, cause = null)
                }
                emit(response.toDomain(responseMessageId, attachmentStorage::saveGenerated))
            }
        }
    }

    private companion object {

        const val NO_IMAGES_MESSAGE = "The model returned no images."
    }
}
