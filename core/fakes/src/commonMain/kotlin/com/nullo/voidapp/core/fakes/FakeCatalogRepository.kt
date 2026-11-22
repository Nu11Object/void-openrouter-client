package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCatalogRepository(
    initialChatModels: List<ChatModel> = emptyList(),
    initialImageModels: List<ImageModel> = emptyList(),
    initialPinnedChatModelIds: Set<String> = emptySet(),
    initialPinnedImageModelIds: Set<String> = emptySet(),
    initialImagePricing: List<ImagePricing> = emptyList(),
) : CatalogRepository {

    private val chatModels = MutableStateFlow(initialChatModels)
    private val imageModels = MutableStateFlow(initialImageModels)
    private val pinnedChatModelIds = MutableStateFlow(initialPinnedChatModelIds)
    private val pinnedImageModelIds = MutableStateFlow(initialPinnedImageModelIds)
    private val imagePricing = MutableStateFlow(initialImagePricing)

    val calls: List<String>
        field = mutableListOf<String>()

    var refreshCatalogFailure: Throwable? = null
    var setPinStatusFailure: Throwable? = null
    var fetchImageModelPricingFailure: Throwable? = null
    var deleteAllFailure: Throwable? = null

    override suspend fun refreshCatalog() {
        calls += "refreshCatalog"
        refreshCatalogFailure?.let { throw it }
    }

    override fun observeChatModels(): Flow<List<ChatModel>> = chatModels

    override fun observeChatModelById(modelId: String): Flow<ChatModel?> =
        chatModels.map { models -> models.firstOrNull { it.id == modelId } }

    override fun observePinnedChatModels(): Flow<List<ChatModel>> =
        combine(chatModels, pinnedChatModelIds) { models, ids ->
            models.filter { it.id in ids }
        }

    override fun observeImageModels(): Flow<List<ImageModel>> = imageModels

    override fun observeImageModelById(modelId: String): Flow<ImageModel?> =
        imageModels.map { models -> models.firstOrNull { it.id == modelId } }

    override fun observePinnedImageModels(): Flow<List<ImageModel>> =
        combine(imageModels, pinnedImageModelIds) { models, ids ->
            models.filter { it.id in ids }
        }

    override suspend fun setPinStatus(modelId: String, modelType: ModelType, pinned: Boolean) {
        calls += "setPinStatus:$modelId:${modelType.name}:$pinned"
        setPinStatusFailure?.let { throw it }
        when (modelType) {
            ModelType.CHAT -> pinnedChatModelIds.update { ids ->
                if (pinned) ids + modelId else ids - modelId
            }

            ModelType.IMAGE -> pinnedImageModelIds.update { ids ->
                if (pinned) ids + modelId else ids - modelId
            }
        }
    }

    override suspend fun fetchImageModelPricing(model: ImageModel): List<ImagePricing> {
        calls += "fetchImageModelPricing:${model.id}"
        fetchImageModelPricingFailure?.let { throw it }
        return imagePricing.value
    }

    override suspend fun deleteAll() {
        calls += "deleteAll"
        deleteAllFailure?.let { throw it }
        chatModels.update { emptyList() }
        imageModels.update { emptyList() }
        pinnedChatModelIds.update { emptySet() }
        pinnedImageModelIds.update { emptySet() }
    }
}
