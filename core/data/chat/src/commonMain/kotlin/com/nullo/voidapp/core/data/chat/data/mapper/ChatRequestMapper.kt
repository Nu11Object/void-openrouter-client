package com.nullo.voidapp.core.data.chat.data.mapper

import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatContentPartDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatMessageDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ImageUrlDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.PluginConfigDto
import com.nullo.voidapp.core.data.chat.data.dto.chat.ReasoningConfigDto
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.chatmodel.TuningParameter

private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"
private const val WEB_PLUGIN_ID = "web"

internal suspend fun buildChatCompletionRequest(
    modelId: String,
    prompt: Message.Prompt,
    history: List<Message>,
    tuningParameters: Map<TuningParameter, Double>,
    thinkingEnabled: Boolean,
    webSearchEnabled: Boolean,
    encodeImage: suspend (fileUri: String, mimeType: String) -> String,
): ChatCompletionRequestDto = ChatCompletionRequestDto(
    model = modelId,
    messages = (history + prompt).toChatMessageDtos(encodeImage),
    stream = true,
    temperature = tuningParameters[TuningParameter.TEMPERATURE],
    topP = tuningParameters[TuningParameter.TOP_P],
    topK = tuningParameters[TuningParameter.TOP_K]?.toInt(),
    maxTokens = tuningParameters[TuningParameter.MAX_TOKENS]?.toInt(),
    frequencyPenalty = tuningParameters[TuningParameter.FREQUENCY_PENALTY],
    presencePenalty = tuningParameters[TuningParameter.PRESENCE_PENALTY],
    repetitionPenalty = tuningParameters[TuningParameter.REPETITION_PENALTY],
    seed = tuningParameters[TuningParameter.SEED]?.toInt(),
    reasoning = ReasoningConfigDto(enabled = thinkingEnabled),
    plugins = listOf(PluginConfigDto(id = WEB_PLUGIN_ID)).takeIf { webSearchEnabled },
)

private suspend fun List<Message>.toChatMessageDtos(
    encodeImage: suspend (String, String) -> String,
): List<ChatMessageDto> = mapNotNull { it.toChatMessageDtoOrNull(encodeImage) }

private suspend fun Message.toChatMessageDtoOrNull(
    encodeImage: suspend (String, String) -> String,
): ChatMessageDto? = when (this) {
    is Message.Prompt -> ChatMessageDto(
        role = ROLE_USER,
        content = buildList {
            if (query.isNotBlank()) add(ChatContentPartDto.Text(query))
            images.forEach { attachment ->
                val url = encodeImage(attachment.relativePath, attachment.mimeType)
                add(ChatContentPartDto.ImageUrl(ImageUrlDto(url = url)))
            }
        },
    )

    is Message.Completion -> ChatMessageDto(
        role = ROLE_ASSISTANT,
        content = listOf(ChatContentPartDto.Text(text)),
    )

    is Message.Images,
    is Message.Error,
    is Message.Interrupted,
    Message.ImageLoading -> null
}
