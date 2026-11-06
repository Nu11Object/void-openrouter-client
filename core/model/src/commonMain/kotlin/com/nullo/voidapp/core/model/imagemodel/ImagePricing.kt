package com.nullo.voidapp.core.model.imagemodel

import androidx.compose.runtime.Immutable
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@Immutable
data class ImagePricing(
    val providerName: String,
    val providerSlug: String,
    val lines: List<PricingLine>,
)

data class PricingLine(
    val billable: String,
    val unit: PricingUnit,
    val costUsd: BigDecimal,
    val variant: String?,
)

enum class PricingUnit {
    REQUEST,
    IMAGE,
    MEGAPIXEL,
    TOKEN,
    OTHER,
}
