package com.nullo.voidapp.core.model.chatmodel

enum class ModelFeature {
    THINKING,
    ;

    companion object {

        fun fromKey(key: String): ModelFeature? = when (key.lowercase()) {
            "reasoning", "include_reasoning", "thinking" -> THINKING
            else -> null
        }
    }
}
