package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.model.imagemodel.ImageModel
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing image models. */
class ObserveImageModelsUseCase(
    private val catalogRepository: CatalogRepository,
) {

    operator fun invoke(): Flow<List<ImageModel>> {
        return catalogRepository.observeImageModels()
    }
}
