package com.nullo.voidapp.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

internal fun testHttpClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(authTestJson)
        }
        install(DefaultRequest) {
            url("https://openrouter.ai/api/v1/")
            contentType(ContentType.Application.Json)
        }
    }
