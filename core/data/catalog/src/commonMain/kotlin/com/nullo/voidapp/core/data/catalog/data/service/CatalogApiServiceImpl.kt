package com.nullo.voidapp.core.data.catalog.data.service

import com.nullo.voidapp.core.data.catalog.data.mapper.toDomain
import com.nullo.voidapp.core.data.catalog.data.service.dto.ChatModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImagePricingResponseDto
import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import kotlin.coroutines.cancellation.CancellationException

internal class CatalogApiServiceImpl(private val httpClient: HttpClient) : CatalogApiService {

    override suspend fun fetchChatModels(): List<ChatModel> = request {
        httpClient.get(LIST_AI_MODELS_ENDPOINT) { expectSuccess = true }
            .body<ChatModelsListResponseDto>()
            .data
            .map { it.toDomain() }
    }

    override suspend fun fetchImageModels(): List<ImageModel> = request {
        httpClient.get(LIST_IMAGE_MODELS_ENDPOINT) { expectSuccess = true }
            .body<ImageModelsListResponseDto>()
            .data
            .map { it.toDomain() }
    }

    override suspend fun fetchImageModelPricing(endpointsUrl: String): List<ImagePricing> = request {
        httpClient.get(endpointsUrl.cutApiVersionPrefix()) { expectSuccess = true }
            .body<ImagePricingResponseDto>()
            .toDomain()
    }

    private suspend fun <T> request(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        throw e.toCatalogException()
    } catch (e: Exception) {
        throw NetworkException(e)
    }

    private fun ResponseException.toCatalogException(): Exception =
        if (response.status.value in INTERNAL_ERROR_STATUSES) {
            InternalServerException(this)
        } else {
            NetworkException(this)
        }

    private fun String.cutApiVersionPrefix() = this.substring(indexOf(LIST_IMAGE_MODELS_ENDPOINT))

    companion object {

        private const val LIST_AI_MODELS_ENDPOINT = "models"
        private const val LIST_IMAGE_MODELS_ENDPOINT = "images/models"
        private val INTERNAL_ERROR_STATUSES = 500..599
    }
}
