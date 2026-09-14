package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.model.ModelType

/** Use case responsible for setting a model's pin status. */
class SetPinStatusUseCase(
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
