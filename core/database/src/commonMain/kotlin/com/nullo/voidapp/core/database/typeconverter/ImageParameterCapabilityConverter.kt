package com.nullo.voidapp.core.database.typeconverter

import androidx.room3.ColumnTypeConverter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import kotlinx.serialization.json.Json

internal object ImageParameterCapabilityConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    fun toString(map: Map<String, ImageParameterCapability>): String {
        return json.encodeToString(map)
    }

    @ColumnTypeConverter
    fun fromString(rawJson: String): Map<String, ImageParameterCapability> {
        if (rawJson.isBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, ImageParameterCapability>>(rawJson)
        }.getOrDefault(emptyMap())
    }
}
