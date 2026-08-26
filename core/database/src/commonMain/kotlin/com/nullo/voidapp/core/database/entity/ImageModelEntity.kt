package com.nullo.voidapp.core.database.entity

import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.nullo.voidapp.core.database.typeconverter.ImageParameterCapabilityConverter
import com.nullo.voidapp.core.database.typeconverter.StringListConverter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability

@Entity(tableName = "image_models")
@ColumnTypeConverters(
    StringListConverter::class,
    ImageParameterCapabilityConverter::class
)
data class ImageModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long,
    val pinned: Boolean = false,
    val endpointsUrl: String,

    val inputModalities: List<String>,
    val outputModalities: List<String>,

    val supportsStreaming: Boolean,
    val supportedParameters: Map<String, ImageParameterCapability>
)
