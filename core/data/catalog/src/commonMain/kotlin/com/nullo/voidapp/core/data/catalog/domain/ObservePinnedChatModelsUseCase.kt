package com.nullo.voidapp.core.data.catalog.domain

import com.nullo.voidapp.core.model.chatmodel.ChatModel
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing **pinned** chat models. */
class ObservePinnedChatModelsUseCase(
    private val catalogRepository: CatalogRepository,
) {

    operator fun invoke(): Flow<List<ChatModel>> {
        return catalogRepository.observePinnedChatModels()
    }
}
