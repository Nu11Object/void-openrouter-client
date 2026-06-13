package com.nullo.voidapp.core.network.di

import com.nullo.voidapp.core.network.client.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { HttpClientFactory(get()).create() }
}
