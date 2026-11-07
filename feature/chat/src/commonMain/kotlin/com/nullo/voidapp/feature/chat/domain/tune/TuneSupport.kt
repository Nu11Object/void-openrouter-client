package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.chatmodel.TuningParameter

internal enum class TuneSupport {
    TEMPERATURE_PRECISE,
    TEMPERATURE_BALANCED,
    TEMPERATURE_WILD,
    TOP_P_NARROW,
    TOP_P_BALANCED,
    TOP_P_FULL,
    TOP_K_OFF,
    TOP_K_FOCUSED,
    TOP_K_DIVERSE,
    PENALTY_ENCOURAGES_REPEATS,
    PENALTY_NEUTRAL,
    PENALTY_MILD,
    PENALTY_STRONG,
}

internal fun TuningParameter.supportFor(value: Double): TuneSupport? = when (this) {
    TuningParameter.TEMPERATURE -> when {
        value <= 0.3 -> TuneSupport.TEMPERATURE_PRECISE
        value <= 1.1 -> TuneSupport.TEMPERATURE_BALANCED
        else -> TuneSupport.TEMPERATURE_WILD
    }

    TuningParameter.TOP_P -> when {
        value <= 0.5 -> TuneSupport.TOP_P_NARROW
        value < 0.95 -> TuneSupport.TOP_P_BALANCED
        else -> TuneSupport.TOP_P_FULL
    }

    TuningParameter.TOP_K -> when {
        value < 1 -> TuneSupport.TOP_K_OFF
        value <= 20 -> TuneSupport.TOP_K_FOCUSED
        else -> TuneSupport.TOP_K_DIVERSE
    }

    TuningParameter.FREQUENCY_PENALTY,
    TuningParameter.PRESENCE_PENALTY,
    -> penaltySupport(offset = value, strongFrom = 1.0)

    TuningParameter.REPETITION_PENALTY -> penaltySupport(offset = value - 1.0, strongFrom = 0.3)

    TuningParameter.MAX_TOKENS,
    TuningParameter.SEED,
    -> null
}

private fun penaltySupport(offset: Double, strongFrom: Double): TuneSupport = when {
    offset < -0.05 -> TuneSupport.PENALTY_ENCOURAGES_REPEATS
    offset <= 0.05 -> TuneSupport.PENALTY_NEUTRAL
    offset <= strongFrom -> TuneSupport.PENALTY_MILD
    else -> TuneSupport.PENALTY_STRONG
}
