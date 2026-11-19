package com.nullo.voidapp.core.data.catalog.data

import com.nullo.voidapp.core.data.catalog.data.service.CatalogApiService
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing

internal class FakeCatalogApiService : CatalogApiService {

    var chatResult: suspend () -> List<ChatModel> = { emptyList() }
    var imageResult: suspend () -> List<ImageModel> = { emptyList() }
    var pricingResult: suspend (String) -> List<ImagePricing> = { emptyList() }

    val pricingUrls = mutableListOf<String>()

    var chatCalls = 0
        private set

    var imageCalls = 0
        private set

    override suspend fun fetchChatModels(): List<ChatModel> {
        chatCalls++
        return chatResult()
    }

    override suspend fun fetchImageModels(): List<ImageModel> {
        imageCalls++
        return imageResult()
    }

    override suspend fun fetchImageModelPricing(endpointsUrl: String): List<ImagePricing> {
        pricingUrls += endpointsUrl
        return pricingResult(endpointsUrl)
    }
}
