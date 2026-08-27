package com.nullo.voidapp.feature.catalog.domain

import com.nullo.voidapp.core.model.ModelType

/** Use case responsible for setting a model's pin status. */
internal class SetPinStatusUseCase(
    private val catalogRepository: CatalogRepository,
) {

    suspend operator fun invoke(
        modelId: String,
        modelType: ModelType,
        pinned: Boolean
    ) {
        catalogRepository.setPinStatus(modelId, modelType, pinned)
    }
}
