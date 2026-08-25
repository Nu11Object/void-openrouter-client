package com.nullo.voidapp.core.model.imagemodel

import com.nullo.voidapp.core.model.aimodel.Modality

data class ImageModel(
    val id: String,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long?,
    val inputModalities: Set<Modality>,
    val outputModalities: Set<Modality>,
    val supportsStreaming: Boolean,
    val supportedParameters: Map<ImageParameter, ImageParameterCapability>
) {

    fun capabilityOf(param: ImageParameter): ImageParameterCapability? = supportedParameters[param]

    fun supports(param: ImageParameter): Boolean = param in supportedParameters
}
