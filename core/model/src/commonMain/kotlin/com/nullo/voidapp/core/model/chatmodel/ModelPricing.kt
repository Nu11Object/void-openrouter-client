package com.nullo.voidapp.core.model.chatmodel

import androidx.compose.runtime.Immutable
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@Immutable
data class ModelPricing(
    val promptPerMillion: BigDecimal,
    val completionPerMillion: BigDecimal,
    val image: BigDecimal? = null,
    val request: BigDecimal? = null,
    val webSearch: BigDecimal? = null,
) {

    val isFree: Boolean
        get() = promptPerMillion.compareTo(BigDecimal.ZERO) == 0 &&
                completionPerMillion.compareTo(BigDecimal.ZERO) == 0
}
