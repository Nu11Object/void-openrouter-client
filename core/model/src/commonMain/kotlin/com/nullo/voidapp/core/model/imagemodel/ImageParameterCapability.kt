package com.nullo.voidapp.core.model.imagemodel

sealed interface ImageParameterCapability {

    data class Options(val values: Set<String>) : ImageParameterCapability

    data class Range(val min: Int, val max: Int) : ImageParameterCapability

    data object Flag : ImageParameterCapability
}
