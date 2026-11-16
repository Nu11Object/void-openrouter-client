package com.nullo.voidapp.core.testing

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability

fun chatModel(
    id: String = "test/chat-model",
    name: String = "Test Model",
    inputModalities: Set<Modality> = emptySet(),
    outputModalities: Set<Modality> = emptySet(),
    supportedFeatures: Set<ModelFeature> = emptySet(),
    supportedTuningParameters: Set<TuningParameter> = emptySet(),
    pricing: ModelPricing = ModelPricing(
        promptPerMillion = 2.toBigDecimal(),
        completionPerMillion = 10.toBigDecimal()
    ),
    contextLength: Int? = 1_000_000,
    maxCompletionTokens: Int? = 256_000,
    pinned: Boolean = false,
) = ChatModel(
    id = id,
    name = name,
    description = "",
    createdAtTimestamp = null,
    contextLength = contextLength,
    maxCompletionTokens = maxCompletionTokens,
    inputModalities = inputModalities,
    outputModalities = outputModalities,
    supportedTuningParameters = supportedTuningParameters,
    defaultTuningParameters = DefaultTuningParameters(),
    supportedFeatures = supportedFeatures,
    pricing = pricing,
    pinned = pinned
)

fun imageModel(
    id: String = "test/image-model",
    supportedParameters: Map<ImageParameter, ImageParameterCapability> = emptyMap(),
    inputModalities: Set<Modality> = emptySet(),
    outputModalities: Set<Modality> = emptySet(),
    pinned: Boolean = false,
) = ImageModel(
    id = id,
    supportedParameters = supportedParameters,
    name = "Test Model",
    description = "",
    createdAtTimestamp = 0L,
    inputModalities = inputModalities,
    outputModalities = outputModalities,
    supportsStreaming = false,
    endpointsUrl = "",
    pinned = pinned
)
