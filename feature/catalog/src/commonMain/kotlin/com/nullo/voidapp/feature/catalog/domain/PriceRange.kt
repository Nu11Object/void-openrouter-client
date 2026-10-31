package com.nullo.voidapp.feature.catalog.domain

import androidx.compose.runtime.Immutable
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@Immutable
data class PriceRange(
    val min: BigDecimal? = null,
    val max: BigDecimal? = null,
) {
    val isUnbounded: Boolean
        get() = min == null && max == null

    fun contains(price: BigDecimal): Boolean =
        (min == null || price >= min) &&
            (max == null || price <= max)
}
