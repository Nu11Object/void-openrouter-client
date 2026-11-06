package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing

/** Use case responsible for fetching the pricing of an image model. */
class GetImageModelPricingUseCase(private val catalogRepository: CatalogRepository) {

    suspend operator fun invoke(model: ImageModel): List<ImagePricing> = catalogRepository.fetchImageModelPricing(model)
}
