package com.nullo.voidapp.core.data.catalog.data.service.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImageModelsListResponseDto(
    @SerialName("data")
    val data: List<ImageModelDto>,
)

@Serializable
internal data class ImageModelDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("created")
    val created: Long,
    @SerialName("architecture")
    val architecture: ImageModelArchitectureDto,
    @SerialName("supported_parameters")
    val supportedParameters: Map<String, ImageParameterCapabilityDto> = emptyMap(),
    @SerialName("supports_streaming")
    val supportsStreaming: Boolean,
    @SerialName("endpoints")
    val endpoints: String,
)

@Serializable
internal data class ImageModelArchitectureDto(
    @SerialName("input_modalities")
    val inputModalities: List<String> = emptyList(),
    @SerialName("output_modalities")
    val outputModalities: List<String> = emptyList(),
)

@Serializable
internal sealed class ImageParameterCapabilityDto {

    @Serializable
    @SerialName("enum")
    internal data class Enum(
        @SerialName("values")
        val values: List<String>,
    ) : ImageParameterCapabilityDto()

    @Serializable
    @SerialName("range")
    internal data class Range(
        @SerialName("min")
        val min: Double,
        @SerialName("max")
        val max: Double,
    ) : ImageParameterCapabilityDto()

    @Serializable
    @SerialName("boolean")
    internal data object Boolean : ImageParameterCapabilityDto()
}
