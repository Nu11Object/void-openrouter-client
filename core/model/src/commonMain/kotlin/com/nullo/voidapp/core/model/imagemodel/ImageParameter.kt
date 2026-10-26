package com.nullo.voidapp.core.model.imagemodel

enum class ImageParameter {
    RESOLUTION,
    ASPECT_RATIO,
    SIZE,
    QUALITY,
    OUTPUT_FORMAT,
    BACKGROUND,
    OUTPUT_COMPRESSION,
    SEED,
    N,
    INPUT_REFERENCES,
    ;

    companion object {

        fun fromKey(key: String): ImageParameter? = when (key.lowercase()) {
            "resolution" -> RESOLUTION
            "aspect_ratio" -> ASPECT_RATIO
            "size" -> SIZE
            "quality" -> QUALITY
            "output_format" -> OUTPUT_FORMAT
            "background" -> BACKGROUND
            "output_compression" -> OUTPUT_COMPRESSION
            "seed" -> SEED
            "n" -> N
            "input_references" -> INPUT_REFERENCES
            else -> null
        }
    }
}
