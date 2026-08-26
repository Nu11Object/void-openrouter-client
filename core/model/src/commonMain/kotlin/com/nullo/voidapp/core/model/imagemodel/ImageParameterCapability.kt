package com.nullo.voidapp.core.model.imagemodel

sealed interface ImageParameterCapability {

    data class Options(val values: Set<String>) : ImageParameterCapability

    data class Range(val min: Double, val max: Double) : ImageParameterCapability

    data object Flag : ImageParameterCapability
}
