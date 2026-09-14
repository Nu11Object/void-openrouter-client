package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException

/**
 * Use case responsible for refreshing the local models catalog from OpenRouter.
 *
 * @throws InternalServerException If the server returned an internal error.
 * @throws NetworkException If the request failed for any other reason
 * (connectivity, timeout, serialization, or an unhandled status code).
 */
class RefreshCatalogUseCase(
    private val catalogRepository: CatalogRepository,
) {

    suspend operator fun invoke() {
        catalogRepository.refreshCatalog()
    }
}
