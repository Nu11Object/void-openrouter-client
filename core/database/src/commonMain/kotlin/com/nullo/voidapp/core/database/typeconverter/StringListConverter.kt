package com.nullo.voidapp.core.database.typeconverter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json

internal object StringListConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    fun toString(list: List<String>): String {
        return json.encodeToString(list)
    }

    @ColumnTypeConverter
    fun fromString(rawJson: String): List<String> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<String>>(rawJson)
        }.getOrDefault(emptyList())
    }
}
