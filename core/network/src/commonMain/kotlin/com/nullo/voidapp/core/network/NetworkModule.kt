package com.nullo.voidapp.core.network

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }
    }
    single<HttpClient> {
        HttpClientFactory(
            apiKeyStorage = get(),
            json = get()
        ).create()
    }
}
