package com.nullo.voidapp.core.data.catalog.service

import com.nullo.voidapp.core.data.catalog.dto.ImageModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.dto.ImagePricingResponseDto
import com.nullo.voidapp.core.data.catalog.dto.ChatModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.mapper.toDomain
import com.nullo.voidapp.core.data.catalog.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.service.exception.NetworkException
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

internal class CatalogApiServiceImpl(
    private val httpClient: HttpClient,
) : CatalogApiService {

    override suspend fun fetchChatModels(): List<ChatModel> {
        try {
            return httpClient.get(LIST_AI_MODELS_ENDPOINT)
                .body<ChatModelsListResponseDto>()
                .data
                .map { it.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            when (e.response.status) {
                HttpStatusCode.InternalServerError -> throw InternalServerException(e)
                else -> throw NetworkException(e)
            }
        } catch (e: Exception) {
            throw NetworkException(e)
        }
    }

    override suspend fun fetchImageModels(): List<ImageModel> {
        try {
            return httpClient.get(LIST_IMAGE_MODELS_ENDPOINT)
                .body<ImageModelsListResponseDto>()
                .data
                .map { it.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            when (e.response.status) {
                HttpStatusCode.InternalServerError -> throw InternalServerException(e)
                else -> throw NetworkException(e)
            }
        } catch (e: Exception) {
            throw NetworkException(e)
        }
    }

    override suspend fun fetchImageModelPrice(endpointsUrl: String): ImagePricing? {
        try {
            return httpClient.get(endpointsUrl.cutApiVersionPrefix())
                .body<ImagePricingResponseDto>()
                .toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            when (e.response.status) {
                HttpStatusCode.InternalServerError -> throw InternalServerException(e)
                else -> throw NetworkException(e)
            }
        } catch (e: Exception) {
            throw NetworkException(e)
        }
    }

    private fun String.cutApiVersionPrefix() = this.substring(indexOf(LIST_IMAGE_MODELS_ENDPOINT))

    companion object {

        const val LIST_AI_MODELS_ENDPOINT = "models"
        const val LIST_IMAGE_MODELS_ENDPOINT = "images/models"
    }
}
