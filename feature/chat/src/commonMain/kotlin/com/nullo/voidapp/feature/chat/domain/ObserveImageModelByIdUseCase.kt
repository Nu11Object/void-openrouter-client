package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing image model by its ID. */
internal class ObserveImageModelByIdUseCase(
    private val catalogRepository: CatalogRepository
) {

    operator fun invoke(modelId: String): Flow<ImageModel?> {
        return catalogRepository.observeImageModelById(modelId)
    }
}
