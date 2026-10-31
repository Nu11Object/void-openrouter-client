package com.nullo.voidapp.feature.catalog.domain

import androidx.compose.runtime.Immutable
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature

@Immutable
data class ChatModelFilters(
    val freeOnly: Boolean = false,
    val pinnedOnly: Boolean = false,
    val inputPrice: PriceRange = PriceRange(),
    val outputPrice: PriceRange = PriceRange(),
    val minContextLength: Int? = null,
    val requiredFeatures: Set<ModelFeature> = emptySet(),
    val requiredInputModalities: Set<Modality> = emptySet(),
    val requiredOutputModalities: Set<Modality> = emptySet(),
) {
    val isDefault: Boolean
        get() = this == Default

    val activeCount: Int
        get() = freeOnly.toInt() +
            pinnedOnly.toInt() +
            (!freeOnly && !inputPrice.isUnbounded).toInt() +
            (!freeOnly && !outputPrice.isUnbounded).toInt() +
            (minContextLength != null).toInt() +
            requiredFeatures.size +
            requiredInputModalities.size +
            requiredOutputModalities.size

    companion object {
        val Default = ChatModelFilters()
    }
}

fun ChatModel.matches(filters: ChatModelFilters): Boolean =
    (!filters.pinnedOnly || pinned) &&
        (!filters.freeOnly || pricing.isFree) &&
        (
            filters.freeOnly || (
                filters.inputPrice.accepts(pricing.promptPerMillion) &&
                    filters.outputPrice.accepts(pricing.completionPerMillion)
                )
            ) &&
        (filters.minContextLength == null || contextLength?.let { it >= filters.minContextLength } ?: false) &&
        supportedFeatures.containsAll(filters.requiredFeatures) &&
        inputModalities.containsAll(filters.requiredInputModalities) &&
        outputModalities.containsAll(filters.requiredOutputModalities)

fun List<ChatModel>.applyFilters(filters: ChatModelFilters): List<ChatModel> =
    if (filters.isDefault) this else filter { it.matches(filters) }

private fun Boolean.toInt(): Int = if (this) 1 else 0

private fun PriceRange.accepts(price: BigDecimal): Boolean =
    if (price < BigDecimal.ZERO) isUnbounded else contains(price)
