package com.nullo.voidapp.core.data.chat.data.service

import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionChunkDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationResponseDto
import com.nullo.voidapp.core.data.chat.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.chat.data.service.exception.NetworkException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json

internal class ChatApiServiceImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : ChatApiService {

    override fun streamChatCompletion(
        request: ChatCompletionRequestDto
    ): Flow<ChatCompletionChunkDto> = callbackFlow {
        try {
            httpClient.sse(
                urlString = CHAT_COMPLETIONS_ENDPOINT,
                request = {
                    method = HttpMethod.Post
                    setBody(request)
                },
            ) {
                incoming
                    .takeWhile { it.data != SSE_DONE }
                    .collect { event ->
                        val raw = event.data ?: return@collect
                        val chunk = json.decodeFromString<ChatCompletionChunkDto>(raw)
                        chunk.error?.let { error(it.message) }
                        trySend(chunk)
                    }
            }
            close()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            close(
                when (e.response.status) {
                    HttpStatusCode.InternalServerError -> InternalServerException(e)
                    else -> NetworkException(e)
                }
            )
        } catch (e: Exception) {
            close(NetworkException(e))
        }

        awaitClose()
    }

    override suspend fun generateImage(request: ImageGenerationRequestDto): ImageGenerationResponseDto {
        try {
            return httpClient.post(IMAGES_ENDPOINT) { setBody(request) }.body()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            throw when (e.response.status) {
                HttpStatusCode.InternalServerError -> InternalServerException(e)
                else -> NetworkException(e)
            }
        } catch (e: Exception) {
            throw NetworkException(e)
        }
    }

    companion object {

        private const val CHAT_COMPLETIONS_ENDPOINT = "chat/completions"
        private const val IMAGES_ENDPOINT = "images"
        private const val SSE_DONE = "[DONE]"
    }
}
