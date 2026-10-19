package com.nullo.voidapp.core.data.catalog.data

import com.nullo.voidapp.core.data.catalog.data.service.CatalogApiService
import com.nullo.voidapp.core.database.dao.ChatModelDao
import com.nullo.voidapp.core.database.dao.ImageModelDao
import com.nullo.voidapp.core.database.mapper.toDomain
import com.nullo.voidapp.core.database.mapper.toEntity
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.utils.kotlin.mapItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class CatalogRepositoryImpl(
    private val chatModelDao: ChatModelDao,
    private val imageModelDao: ImageModelDao,
    private val catalogApiService: CatalogApiService,
) : CatalogRepository {

    override suspend fun refreshCatalog() = withContext(Dispatchers.IO) {
        val chatModels = async { catalogApiService.fetchChatModels() }
        val imageModels = async { catalogApiService.fetchImageModels() }
        chatModelDao.replaceAll(chatModels.await().map { it.toEntity() })
        imageModelDao.replaceAll(imageModels.await().map { it.toEntity() })
    }

    override fun observeChatModels(): Flow<List<ChatModel>> = chatModelDao
        .observeAll()
        .mapItems { it.toDomain() }

    override fun observeChatModelById(modelId: String) : Flow<ChatModel?> = chatModelDao
        .observeById(modelId)
        .map { it?.toDomain() }

    override fun observeImageModels(): Flow<List<ImageModel>> = imageModelDao
        .observeAll()
        .mapItems { it.toDomain() }

    override fun observeImageModelById(modelId: String) : Flow<ImageModel?> = imageModelDao
        .observeById(modelId)
        .map { it?.toDomain() }

    override fun observePinnedChatModels(): Flow<List<ChatModel>> = chatModelDao
        .observePinned()
        .mapItems { it.toDomain() }

    override fun observePinnedImageModels(): Flow<List<ImageModel>> = imageModelDao
        .observePinned()
        .mapItems { it.toDomain() }

    override suspend fun setPinStatus(modelId: String, modelType: ModelType, pinned: Boolean) {
        withContext(Dispatchers.IO) {
            when (modelType) {
                ModelType.CHAT -> chatModelDao.setPinned(modelId, pinned)
                ModelType.IMAGE -> imageModelDao.setPinned(modelId, pinned)
            }
        }
    }

    override suspend fun deleteAll() {
        chatModelDao.deleteAll()
        imageModelDao.deleteAll()
    }
}
