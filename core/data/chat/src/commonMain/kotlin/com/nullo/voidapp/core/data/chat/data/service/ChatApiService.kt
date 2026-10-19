package com.nullo.voidapp.core.data.chat.data.service

import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionChunkDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationResponseDto
import com.nullo.voidapp.core.data.chat.data.mapper.buildChatCompletionRequest
import com.nullo.voidapp.core.data.chat.data.mapper.buildImageGenerationRequest
import com.nullo.voidapp.core.data.chat.data.mapper.toCompletionMessageFlow
import kotlinx.coroutines.flow.Flow

interface ChatApiService {

    /**
     * Streams a chat completion over SSE. Each element is a raw chunk; accumulating
     * chunks into message text is up to the caller (see [toCompletionMessageFlow]).
     *
     * @throws UnauthorizedException if the API key is invalid or missing
     * @throws InsufficientCreditsException if the account has insufficient credits.
     * @throws RateLimitedException if the request was rate limited.
     * @throws InternalServerException if the server or the upstream provider failed.
     * @throws NetworkException if the request failed for any other reason.
     * @see [buildChatCompletionRequest]
     */
    fun streamChatCompletion(request: ChatCompletionRequestDto): Flow<ChatCompletionChunkDto>

    /**
     * Generates images in a single buffered request.
     *
     * @throws UnauthorizedException if the API key is invalid or missing
     * @throws InsufficientCreditsException if the account has insufficient credits.
     * @throws RateLimitedException if the request was rate limited.
     * @throws InternalServerException if the server or the upstream provider failed.
     * @throws NetworkException if the request failed for any other reason.
     * @see [buildImageGenerationRequest]
     */
    suspend fun generateImage(request: ImageGenerationRequestDto): ImageGenerationResponseDto
}
