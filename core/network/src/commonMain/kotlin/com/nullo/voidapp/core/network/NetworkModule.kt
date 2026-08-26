package com.nullo.voidapp.core.network

import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { HttpClientFactory(get()).create() }
}
