package com.nullo.voidapp.core.data.chat.data.mapper

import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationRequestDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageGenerationResponseDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageReferenceDto
import com.nullo.voidapp.core.data.chat.data.dto.image.ImageReferenceUrlDto
import com.nullo.voidapp.core.model.GeneratedImage
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue

internal fun buildImageGenerationRequest(
    modelId: String,
    prompt: String,
    referenceImages: List<String>,
    parameters: Map<ImageParameter, ImageParameterValue>,
): ImageGenerationRequestDto {
    fun text(param: ImageParameter) = (parameters[param] as? ImageParameterValue.Text)?.value
    fun number(param: ImageParameter) = (parameters[param] as? ImageParameterValue.Number)?.value

    return ImageGenerationRequestDto(
        model = modelId,
        prompt = prompt,
        n = number(ImageParameter.N)?.toInt(),
        resolution = text(ImageParameter.RESOLUTION),
        aspectRatio = text(ImageParameter.ASPECT_RATIO),
        size = text(ImageParameter.SIZE),
        quality = text(ImageParameter.QUALITY),
        outputFormat = text(ImageParameter.OUTPUT_FORMAT),
        background = text(ImageParameter.BACKGROUND),
        outputCompression = number(ImageParameter.OUTPUT_COMPRESSION)?.toInt(),
        seed = number(ImageParameter.SEED)?.toInt(),
        inputReferences = referenceImages
            .takeIf { it.isNotEmpty() }
            ?.map { ImageReferenceDto(imageUrl = ImageReferenceUrlDto(url = it)) }
    )
}

internal fun ImageGenerationResponseDto.toDomain(
    messageId: Long,
    caption: String?,
): Message.Images = Message.Images(
    id = messageId,
    caption = caption,
    images = data.map { imageDto ->
        GeneratedImage(
            base64Data = imageDto.b64Json,
            mimeType = imageDto.mediaType ?: "image/png"
        )
    }
)
