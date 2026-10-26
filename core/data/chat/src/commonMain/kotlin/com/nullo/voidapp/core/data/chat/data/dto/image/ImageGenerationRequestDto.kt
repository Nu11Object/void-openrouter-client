package com.nullo.voidapp.core.data.chat.data.dto.image

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageGenerationRequestDto(
    @SerialName("model")
    val model: String,
    @SerialName("prompt")
    val prompt: String,
    @SerialName("n")
    val n: Int? = null,
    @SerialName("resolution")
    val resolution: String? = null,
    @SerialName("aspect_ratio")
    val aspectRatio: String? = null,
    @SerialName("size")
    val size: String? = null,
    @SerialName("quality")
    val quality: String? = null,
    @SerialName("output_format")
    val outputFormat: String? = null,
    @SerialName("background")
    val background: String? = null,
    @SerialName("output_compression")
    val outputCompression: Int? = null,
    @SerialName("seed")
    val seed: Int? = null,
    @SerialName("input_references")
    val inputReferences: List<ImageReferenceDto>? = null,
)

@Serializable
data class ImageReferenceDto(
    @SerialName("type")
    val type: String = "image_url",
    @SerialName("image_url")
    val imageUrl: ImageReferenceUrlDto,
)

@Serializable
data class ImageReferenceUrlDto(@SerialName("url") val url: String)
