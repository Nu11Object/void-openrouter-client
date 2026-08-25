package com.nullo.voidapp.core.model.imagemodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class ImagePricing(
    val lines: List<PricingLine>
)

data class PricingLine(
    val billable: String,
    val unit: PricingUnit,
    val costUsd: BigDecimal,
    val variant: String?
)

enum class PricingUnit {
    IMAGE,
    MEGAPIXEL,
    TOKEN
}
