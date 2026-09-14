package com.nullo.voidapp.core.data.catalog.data.mapper

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImagePricingEntryDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImagePricingResponseDto
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import com.nullo.voidapp.core.model.imagemodel.PricingLine
import com.nullo.voidapp.core.model.imagemodel.PricingUnit

internal fun ImagePricingResponseDto.toDomain(): ImagePricing? {
    val endpoint = endpoints.firstOrNull() ?: return null
    return ImagePricing(
        providerName = endpoint.providerName,
        providerSlug = endpoint.providerSlug,
        lines = endpoint.pricing.map { it.toDomain() }
    )
}

private fun ImagePricingEntryDto.toDomain(): PricingLine = PricingLine(
    billable = billable,
    unit = unit.toPricingUnit(),
    costUsd = costUsd.toBigDecimal(),
    variant = variant
)

private fun String.toPricingUnit(): PricingUnit = when (this.lowercase()) {
    "request" -> PricingUnit.REQUEST
    "image" -> PricingUnit.IMAGE
    "megapixel" -> PricingUnit.MEGAPIXEL
    "token" -> PricingUnit.TOKEN
    else -> PricingUnit.IMAGE
}

private fun Double.toBigDecimal(): BigDecimal = BigDecimal.parseString(this.toString())
