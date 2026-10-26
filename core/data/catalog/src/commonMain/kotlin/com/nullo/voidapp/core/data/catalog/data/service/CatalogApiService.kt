package com.nullo.voidapp.core.data.catalog.data.service

import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing

/**
 * Provides access to the OpenRouter model catalog: chat models, image generation
 * models, and pricing for a specific image model.
 */
interface CatalogApiService {

    /**
     * Fetches the full list of chat-capable models available on OpenRouter.
     *
     * @throws InternalServerException the server returned an internal error.
     * @throws NetworkException If the request failed for any other reason
     * (connectivity, timeout, serialization, or an unhandled status code).
     */
    suspend fun fetchChatModels(): List<ChatModel>

    /**
     * Fetches the full list of dedicated image generation models available on OpenRouter.
     *
     * @throws InternalServerException If the server returned an internal error.
     * @throws NetworkException If the request failed for any other reason
     * (connectivity, timeout, serialization, or an unhandled status code).
     */
    suspend fun fetchImageModels(): List<ImageModel>

    /**
     * Fetches pricing for a specific image model from its endpoint records.
     *
     * A model may be served by multiple providers with different pricing; this
     * returns the first available provider's pricing, or `null` if the model
     * currently has no active providers.
     *
     * @param endpointsUrl The relative endpoints URL reported for the model
     * (see [ImageModel.endpointsUrl]).
     * @throws InternalServerException If the server returned an internal error.
     * @throws NetworkException If the request failed for any other reason
     * (connectivity, timeout, serialization, or an unhandled status code).
     */
    suspend fun fetchImageModelPrice(endpointsUrl: String): ImagePricing?
}
