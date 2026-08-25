package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.ImageModelEntity
import com.nullo.voidapp.core.model.aimodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter

fun ImageModelEntity.toDomain(): ImageModel = ImageModel(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    inputModalities = inputModalities.mapNotNull { Modality.fromKey(it) }.toSet(),
    outputModalities = outputModalities.mapNotNull { Modality.fromKey(it) }.toSet(),
    supportsStreaming = supportsStreaming,
    supportedParameters = supportedParameters.mapNotNull { (key, capability) ->
        val param = ImageParameter.fromKey(key) ?: return@mapNotNull null
        param to capability
    }.toMap()
)

fun ImageModel.toEntity(): ImageModelEntity = ImageModelEntity(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    inputModalities = inputModalities.map { it.name.lowercase() },
    outputModalities = outputModalities.map { it.name.lowercase() },
    supportsStreaming = supportsStreaming,
    supportedParameters = supportedParameters.mapKeys { (param, _) -> param.name.lowercase() }
)
