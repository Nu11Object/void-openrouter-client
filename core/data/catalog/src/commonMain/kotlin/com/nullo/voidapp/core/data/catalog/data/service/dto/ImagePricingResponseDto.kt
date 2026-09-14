package com.nullo.voidapp.core.data.catalog.data.service.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImagePricingResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("endpoints")
    val endpoints: List<ImageEndpointDto>
)

@Serializable
internal data class ImageEndpointDto(
    @SerialName("provider_name")
    val providerName: String,
    @SerialName("provider_slug")
    val providerSlug: String,
    @SerialName("pricing")
    val pricing: List<ImagePricingEntryDto>
)

@Serializable
internal data class ImagePricingEntryDto(
    @SerialName("billable")
    val billable: String,
    @SerialName("unit")
    val unit: String,
    @SerialName("cost_usd")
    val costUsd: Double,
    @SerialName("variant")
    val variant: String? = null
)
