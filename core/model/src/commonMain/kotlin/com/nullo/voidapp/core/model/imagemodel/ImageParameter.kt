package com.nullo.voidapp.core.model.imagemodel

enum class ImageParameter(val key: String) {
    RESOLUTION("resolution"),
    ASPECT_RATIO("aspect_ratio"),
    SIZE("size"),
    QUALITY("quality"),
    OUTPUT_FORMAT("output_format"),
    BACKGROUND("background"),
    OUTPUT_COMPRESSION("output_compression"),
    SEED("seed"),
    N("n"),
    INPUT_REFERENCES("input_references"),
    ;

    companion object {

        fun fromKey(key: String): ImageParameter? = entries.firstOrNull { it.key == key.lowercase() }
    }
}
