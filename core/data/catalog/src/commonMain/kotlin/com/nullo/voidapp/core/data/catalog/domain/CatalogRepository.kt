package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import kotlinx.coroutines.flow.Flow

/**
 * Manages the locally cached model catalog: syncing it with OpenRouter,
 * observing it (filtered by the current search query), and tracking which
 * models are pinned.
 */
interface CatalogRepository {

    /**
     * Fetches the full chat and image model lists from OpenRouter and replaces
     * the local cache with them.
     *
     * @throws InternalServerException If the server returned an internal error.
     * @throws NetworkException If the request failed for any other reason
     * (connectivity, timeout, serialization, or an unhandled status code).
     */
    suspend fun refreshCatalog()

    /**
     * Observes cached chat models.
     * @see [refreshCatalog]
     */
    fun observeChatModels(): Flow<List<ChatModel>>

    /**
     * Observes chat model by ID.
     */
    fun observeChatModelById(modelId: String): Flow<ChatModel?>

    /**
     * Observes pinned chat models.
     */
    fun observePinnedChatModels(): Flow<List<ChatModel>>

    /**
     * Observes cached image generation models.
     * @see [refreshCatalog]
     */
    fun observeImageModels(): Flow<List<ImageModel>>

    /**
     * Observes image model by ID.
     */
    fun observeImageModelById(modelId: String): Flow<ImageModel?>

    /**
     * Observes pinned image models.
     */
    fun observePinnedImageModels(): Flow<List<ImageModel>>

    /**
     * Sets the model's pin status.
     *
     * The pinned status is preserved after [refreshCatalog], unless the model
     * is no longer supported by OpenRouter.
     */
    suspend fun setPinStatus(
        modelId: String,
        modelType: ModelType,
        pinned: Boolean,
    )

    /** Deletes all Chat and Image models. */
    suspend fun deleteAll()
}
