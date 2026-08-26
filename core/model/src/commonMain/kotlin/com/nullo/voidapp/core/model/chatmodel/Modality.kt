package com.nullo.voidapp.core.model.chatmodel

enum class Modality {
    TEXT,
    IMAGE;
    // Consider VIDEO, AUDIO in future

    companion object {

        fun fromKey(key: String): Modality? = when (key.lowercase()) {
            "text" -> TEXT
            "image" -> IMAGE
            else -> null
        }
    }
}
