package com.nullo.voidapp.core.network.client

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.utils.kotlin.withBearerPrefix
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class HttpClientFactory(
    private val apiKeyStorage: ApiKeyStorage,
) {

    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json = Json { ignoreUnknownKeys = true })
        }

        install(Logging) {
            level = LogLevel.ALL
            sanitizeHeader { it == HttpHeaders.Authorization }
        }

        install(DefaultRequest) {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            socketTimeoutMillis = SOCKET_TIMEOUT_MS
        }

        install(apiKeyPlugin)
    }

    private val apiKeyPlugin = createClientPlugin("apiKeyPlugin") {
        onRequest { request, _ ->
            if (request.headers.contains(HttpHeaders.Authorization)) return@onRequest

            runCatching { apiKeyStorage.getApiKey() }.getOrNull()?.let { key ->
                request.header(HttpHeaders.Authorization, key.withBearerPrefix())
            }
        }
    }

    companion object {

        private const val BASE_URL = "https://openrouter.ai/api/v1/"
        private const val CONNECT_TIMEOUT_MS = 5_000L
        private const val SOCKET_TIMEOUT_MS = 30_000L
    }
}
