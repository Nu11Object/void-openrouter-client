package com.nullo.voidapp.core.model.chatmodel

import androidx.compose.runtime.Immutable

@Immutable
data class DefaultTuningParameters(
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val frequencyPenalty: Double? = null,
    val presencePenalty: Double? = null,
    val repetitionPenalty: Double? = null,
)
