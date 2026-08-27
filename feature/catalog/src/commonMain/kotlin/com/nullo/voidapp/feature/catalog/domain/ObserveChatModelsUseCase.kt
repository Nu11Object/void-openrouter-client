package com.nullo.voidapp.feature.catalog.domain

import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing chat models. */
internal class ObserveChatModelsUseCase(
    private val catalogRepository: CatalogRepository,
) {

    operator fun invoke(): Flow<List<ChatModel>> {
        return catalogRepository.observeChatModels()
    }
}
