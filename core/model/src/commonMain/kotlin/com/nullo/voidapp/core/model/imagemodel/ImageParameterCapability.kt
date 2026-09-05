package com.nullo.voidapp.core.model.imagemodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ImageParameterCapability {
    @Serializable
    @SerialName("options")
    data class Options(val values: Set<String>) : ImageParameterCapability

    @Serializable
    @SerialName("range")
    data class Range(val min: Double, val max: Double) : ImageParameterCapability

    @Serializable
    @SerialName("flag")
    data object Flag : ImageParameterCapability
}
