package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing chat model by its ID. */
internal class ObserveChatModelByIdUseCase(
    private val catalogRepository: CatalogRepository
) {

    operator fun invoke(modelId: String): Flow<ChatModel?> {
        return catalogRepository.observeChatModelById(modelId)
    }
}
