package com.nullo.voidapp.core.model.chatmodel

enum class TuningParameter(val key: String) {
    TEMPERATURE("temperature"),
    TOP_P("top_p"),
    TOP_K("top_k"),
    MAX_TOKENS("max_tokens"),
    FREQUENCY_PENALTY("frequency_penalty"),
    PRESENCE_PENALTY("presence_penalty"),
    REPETITION_PENALTY("repetition_penalty"),
    SEED("seed"),
    ;

    companion object {

        fun fromKey(key: String): TuningParameter? = entries.firstOrNull { it.key == key.lowercase() }
    }
}
