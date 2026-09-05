package com.nullo.voidapp.core.data.catalog.mapper

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.nullo.voidapp.core.data.catalog.dto.DefaultTuningParametersDto
import com.nullo.voidapp.core.data.catalog.dto.ModelDto
import com.nullo.voidapp.core.data.catalog.dto.PricingDto
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.chatmodel.TuningParameter

private val PRICE_SCALE_PER_MILLION: BigDecimal = BigDecimal.fromInt(1_000_000)

internal fun ModelDto.toDomain(): ChatModel = ChatModel(
    id = id,
    name = name,
    description = description.orEmpty(),
    createdAtTimestamp = created,
    contextLength = contextLength,
    maxCompletionTokens = topProvider?.maxCompletionTokens,
    inputModalities = architecture.inputModalities
        .mapNotNull { Modality.fromKey(it) }
        .toSet(),
    outputModalities = architecture.outputModalities
        .mapNotNull { Modality.fromKey(it) }
        .toSet(),
    supportedTuningParameters = supportedParameters
        .mapNotNull { TuningParameter.fromKey(it) }
        .toSet(),
    supportedFeatures = supportedParameters
        .mapNotNull { ModelFeature.fromKey(it) }
        .toSet(),
    pricing = pricing.toDomain(),
    defaultTuningParameters = defaultParameters?.toDomain() ?: DefaultTuningParameters(),
    pinned = false,
)

private fun PricingDto.toDomain(): ModelPricing = ModelPricing(
    // price per token -> price per 1M tokens
    promptPerMillion = prompt.parsePriceOrZero().multiply(PRICE_SCALE_PER_MILLION),
    completionPerMillion = completion.parsePriceOrZero().multiply(PRICE_SCALE_PER_MILLION),
    // price per 1 action, no scaling
    image = image.parsePriceOrNull(),
    request = request.parsePriceOrNull(),
    webSearch = webSearch.parsePriceOrNull()
)

private fun DefaultTuningParametersDto.toDomain(): DefaultTuningParameters =
    DefaultTuningParameters(
        temperature = temperature,
        topP = topP,
        topK = topK,
        frequencyPenalty = frequencyPenalty,
        presencePenalty = presencePenalty,
        repetitionPenalty = repetitionPenalty,
    )

private fun String.parsePriceOrZero(): BigDecimal = runCatching {
    BigDecimal.parseString(this.trim())
}.getOrDefault(BigDecimal.ZERO)

private fun String?.parsePriceOrNull(): BigDecimal? {
    if (this.isNullOrBlank()) return null
    return runCatching { BigDecimal.parseString(this.trim()) }.getOrNull()
}
