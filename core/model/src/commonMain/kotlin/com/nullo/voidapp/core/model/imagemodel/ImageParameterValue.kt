package com.nullo.voidapp.core.model.imagemodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ImageParameterValue {

    @Serializable
    @SerialName("text")
    data class Text(val value: String) : ImageParameterValue

    @Serializable
    @SerialName("number")
    data class Number(val value: Double) : ImageParameterValue
}
