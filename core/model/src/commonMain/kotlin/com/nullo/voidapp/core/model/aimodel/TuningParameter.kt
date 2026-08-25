package com.nullo.voidapp.core.model.aimodel

enum class TuningParameter {
    TEMPERATURE,
    TOP_P,
    TOP_K,
    MAX_TOKENS,
    FREQUENCY_PENALTY,
    PRESENCE_PENALTY,
    REPETITION_PENALTY,
    SEED;

    companion object {

        fun fromKey(key: String): TuningParameter? = when (key.lowercase()) {
            "temperature" -> TEMPERATURE
            "top_p" -> TOP_P
            "top_k" -> TOP_K
            "max_tokens" -> MAX_TOKENS
            "frequency_penalty" -> FREQUENCY_PENALTY
            "presence_penalty" -> PRESENCE_PENALTY
            "repetition_penalty" -> REPETITION_PENALTY
            "seed" -> SEED
            else -> null
        }
    }
}
