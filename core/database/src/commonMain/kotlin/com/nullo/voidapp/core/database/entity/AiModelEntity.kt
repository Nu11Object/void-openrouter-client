package com.nullo.voidapp.core.database.entity

import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.nullo.voidapp.core.database.typeconverter.StringListConverter

@Entity(tableName = "ai_models")
@ColumnTypeConverters(StringListConverter::class)
data class AiModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long?,
    val contextLength: Int?,
    val maxCompletionTokens: Int?,
    val pinned: Boolean = false,

    val inputModalities: List<String>,
    val outputModalities: List<String>,
    val supportedTuningParameters: List<String>,
    val supportedFeatures: List<String>,

    val promptPerMillion: String,
    val completionPerMillion: String,
    val image: String?,
    val request: String?,
    val webSearch: String?,

    val defaultTemperature: Double?,
    val defaultTopP: Double?,
    val defaultTopK: Int?,
    val defaultFrequencyPenalty: Double?,
    val defaultPresencePenalty: Double?,
    val defaultRepetitionPenalty: Double?,
)
