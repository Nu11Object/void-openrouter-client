package com.nullo.voidapp.core.database.mapper

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.nullo.voidapp.core.database.entity.ChatModelEntity
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.chatmodel.TuningParameter

fun ChatModelEntity.toDomain(): ChatModel = ChatModel(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    contextLength = contextLength,
    maxCompletionTokens = maxCompletionTokens,
    inputModalities = inputModalities
        .mapNotNull { Modality.fromKey(it) }
        .toSet(),
    outputModalities = outputModalities
        .mapNotNull { Modality.fromKey(it) }
        .toSet(),
    supportedTuningParameters = supportedTuningParameters
        .mapNotNull { TuningParameter.fromKey(it) }
        .toSet(),
    supportedFeatures = supportedFeatures
        .mapNotNull { ModelFeature.fromKey(it) }
        .toSet(),
    pricing = ModelPricing(
        promptPerMillion = promptPerMillion.toBigDecimalOrNull() ?: BigDecimal.ZERO,
        completionPerMillion = completionPerMillion.toBigDecimalOrNull() ?: BigDecimal.ZERO,
        image = image.toBigDecimalOrNull(),
        request = request.toBigDecimalOrNull(),
        webSearch = webSearch.toBigDecimalOrNull()
    ),
    defaultTuningParameters = this.toDefaultParameters(),
    pinned = pinned
)

fun ChatModel.toEntity(): ChatModelEntity = ChatModelEntity(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    contextLength = contextLength,
    maxCompletionTokens = maxCompletionTokens,
    inputModalities = inputModalities.map { it.name.lowercase() },
    outputModalities = outputModalities.map { it.name.lowercase() },
    supportedTuningParameters = supportedTuningParameters.map { it.name.lowercase() },
    supportedFeatures = supportedFeatures.map { it.name.lowercase() },
    promptPerMillion = pricing.promptPerMillion.toPlainString(),
    completionPerMillion = pricing.completionPerMillion.toPlainString(),
    image = pricing.image?.toPlainString(),
    request = pricing.request?.toPlainString(),
    webSearch = pricing.webSearch?.toPlainString(),
    defaultTemperature = defaultTuningParameters.temperature,
    defaultTopP = defaultTuningParameters.topP,
    defaultTopK = defaultTuningParameters.topK,
    defaultFrequencyPenalty = defaultTuningParameters.frequencyPenalty,
    defaultPresencePenalty = defaultTuningParameters.presencePenalty,
    defaultRepetitionPenalty = defaultTuningParameters.repetitionPenalty
)

private fun ChatModelEntity.toDefaultParameters(): DefaultTuningParameters =
    DefaultTuningParameters(
        temperature = defaultTemperature,
        topP = defaultTopP,
        topK = defaultTopK,
        frequencyPenalty = defaultFrequencyPenalty,
        presencePenalty = defaultPresencePenalty,
        repetitionPenalty = defaultRepetitionPenalty
    )

private fun String?.toBigDecimalOrNull(): BigDecimal? {
    if (this.isNullOrBlank()) return null
    return runCatching { BigDecimal.parseString(this.trim()) }.getOrNull()
}
