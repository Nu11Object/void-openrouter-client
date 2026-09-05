package com.nullo.voidapp.core.database.typeconverter

import androidx.room3.ColumnTypeConverter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal object ImageParameterCapabilityConverter {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = MapSerializer(
        String.serializer(),
        ImageParameterCapability.serializer()
    )

    @ColumnTypeConverter
    fun toString(map: Map<String, ImageParameterCapability>): String =
        json.encodeToString(serializer, map)

    @ColumnTypeConverter
    fun fromString(rawJson: String): Map<String, ImageParameterCapability> {
        if (rawJson.isBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString(serializer, rawJson)
        }.getOrDefault(emptyMap())
    }
}
