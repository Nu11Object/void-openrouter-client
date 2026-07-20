package com.nullo.voidapp.core.network.client

import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkClientModule = module {
    single<HttpClient> { HttpClientFactory(get()).create() }
}
