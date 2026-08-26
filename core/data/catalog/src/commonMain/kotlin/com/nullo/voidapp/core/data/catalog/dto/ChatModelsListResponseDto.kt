package com.nullo.voidapp.core.data.catalog.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatModelsListResponseDto(
    @SerialName("data")
    val data: List<ModelDto> = emptyList()
)

@Serializable
internal data class ModelDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("created")
    val created: Long? = null,
    @SerialName("context_length")
    val contextLength: Int? = null,
    @SerialName("architecture")
    val architecture: ModelArchitectureDto = ModelArchitectureDto(),
    @SerialName("top_provider")
    val topProvider: TopProviderDto? = null,
    @SerialName("pricing")
    val pricing: PricingDto,
    @SerialName("supported_parameters")
    val supportedParameters: List<String> = emptyList(),
    @SerialName("default_parameters")
    val defaultParameters: DefaultTuningParametersDto? = null
)

@Serializable
internal data class ModelArchitectureDto(
    @SerialName("input_modalities")
    val inputModalities: List<String> = emptyList(),
    @SerialName("output_modalities")
    val outputModalities: List<String> = emptyList()
)

@Serializable
internal data class TopProviderDto(
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null
)

@Serializable
internal data class PricingDto(
    @SerialName("prompt")
    val prompt: String,
    @SerialName("completion")
    val completion: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("request")
    val request: String? = null,
    @SerialName("web_search")
    val webSearch: String? = null
)

@Serializable
internal data class DefaultTuningParametersDto(
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Double? = null,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Double? = null
)