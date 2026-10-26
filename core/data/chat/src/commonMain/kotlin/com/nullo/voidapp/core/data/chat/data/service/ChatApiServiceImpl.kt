package com.nullo.voidapp.core.data.chat.data.service

import com.nullo.voidapp.core.data.chat.data.dto.chat.ApiErrorDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ApiErrorEnvelopeDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionChunkDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

internal class ChatApiServiceImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : ChatApiService {

    override fun streamChatCompletion(
        request: ChatCompletionRequestDto,
    ): Flow<ChatCompletionChunkDto> = callbackFlow {
        try {
            httpClient.sse(
                urlString = CHAT_COMPLETIONS_ENDPOINT,
                request = {
                    method = HttpMethod.Post
                    setBody(request)
                }
            ) {
                incoming
                    .takeWhile { it.data != SSE_DONE }
                    .collect { event ->
                        val raw = event.data ?: return@collect
                        val chunk = json.decodeFromString<ChatCompletionChunkDto>(raw)
                        chunk.error?.let {
                            throw apiException(error = it, httpStatus = null, cause = null)
                        }
                        trySend(chunk)
                    }
            }
            close()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ChatApiException) {
            close(e)
        } catch (e: ResponseException) {
            close(e.toApiException())
        } catch (e: SSEClientException) {
            close(e.response.toApiException(cause = e))
        } catch (e: Exception) {
            close(NetworkException(rawMessage = null, cause = e))
        }

        awaitClose()
    }

    override suspend fun generateImage(request: ImageGenerationRequestDto): ImageGenerationResponseDto {
        try {
            return httpClient.post(IMAGES_ENDPOINT) { setBody(request) }.body()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            throw e.toApiException()
        } catch (e: Exception) {
            throw NetworkException(rawMessage = null, cause = e)
        }
    }

    private suspend fun HttpResponse?.toApiException(cause: Throwable): ChatApiException {
        val error = try {
            this?.let { json.decodeFromString<ApiErrorEnvelopeDto>(it.bodyAsText()).error }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return apiException(error = error, httpStatus = this?.status?.value, cause = cause)
    }

    private suspend fun ResponseException.toApiException(): ChatApiException {
        val error = try {
            json.decodeFromString<ApiErrorEnvelopeDto>(response.bodyAsText()).error
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return apiException(error = error, httpStatus = response.status.value, cause = this)
    }

    companion object {

        private const val CHAT_COMPLETIONS_ENDPOINT = "chat/completions"
        private const val IMAGES_ENDPOINT = "images"
        private const val SSE_DONE = "[DONE]"
    }
}

/**
 * Maps an OpenRouter error to an exception. The status comes from the HTTP response,
 * or from the `code` field when the error arrives as a mid-stream event.
 */
private fun apiException(
    error: ApiErrorDto?,
    httpStatus: Int?,
    cause: Throwable?,
): ChatApiException {
    val rawMessage = error?.message
    val status = httpStatus ?: error?.code?.intOrNull
    return when {
        status == 401 -> UnauthorizedException(rawMessage, cause)
        status == 402 -> InsufficientCreditsException(rawMessage, cause)
        status == 429 -> RateLimitedException(rawMessage, cause)
        status != null && status in 500..599 -> InternalServerException(rawMessage, cause)
        error?.code?.contentOrNull == "server_error" -> InternalServerException(rawMessage, cause)
        else -> NetworkException(rawMessage, cause)
    }
}
