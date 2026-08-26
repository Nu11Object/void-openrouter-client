package com.nullo.voidapp.core.model.chatmodel

data class ChatModel(
    val id: String,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long?,
    val contextLength: Int?,
    val maxCompletionTokens: Int?,
    val inputModalities: Set<Modality>,
    val outputModalities: Set<Modality>,
    val supportedTuningParameters: Set<TuningParameter>,
    val defaultTuningParameters: DefaultTuningParameters,
    val supportedFeatures: Set<ModelFeature>,
    val pricing: ModelPricing
) {

    val supportsReasoning: Boolean
        get() = supportedFeatures.contains(ModelFeature.THINKING)

    val supportsVision: Boolean
        get() = inputModalities.contains(Modality.IMAGE)

    val canGenerateImage: Boolean
        get() = outputModalities.contains(Modality.IMAGE)

    fun supports(tunParam: TuningParameter): Boolean = tunParam in supportedTuningParameters
}
