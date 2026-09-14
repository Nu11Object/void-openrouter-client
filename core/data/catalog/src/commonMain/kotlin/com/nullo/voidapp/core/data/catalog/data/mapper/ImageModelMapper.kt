package com.nullo.voidapp.core.data.catalog.data.mapper

import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageModelDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageParameterCapabilityDto
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability

internal fun ImageModelDto.toDomain(): ImageModel = ImageModel(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = created,
    inputModalities = architecture.inputModalities.mapNotNull { Modality.fromKey(it) }.toSet(),
    outputModalities = architecture.outputModalities.mapNotNull { Modality.fromKey(it) }.toSet(),
    supportsStreaming = supportsStreaming,
    supportedParameters = supportedParameters
        .mapNotNull { (key, dto) ->
            val param = ImageParameter.fromKey(key) ?: return@mapNotNull null
            param to dto.toDomain()
        }
        .toMap(),
    endpointsUrl = endpoints,
    pinned = false
)

private fun ImageParameterCapabilityDto.toDomain(): ImageParameterCapability = when (this) {
    is ImageParameterCapabilityDto.Enum -> ImageParameterCapability.Options(values.toSet())
    is ImageParameterCapabilityDto.Range -> ImageParameterCapability.Range(min, max)
    is ImageParameterCapabilityDto.Boolean -> ImageParameterCapability.Flag
}
