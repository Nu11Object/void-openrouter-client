package com.nullo.voidapp.core.network.auth.di

import com.nullo.voidapp.core.network.auth.service.AuthApiService
import com.nullo.voidapp.core.network.auth.service.AuthApiServiceImpl
import org.koin.dsl.module

val networkAuthModule = module {
    single<AuthApiService> { AuthApiServiceImpl(get()) }
}
