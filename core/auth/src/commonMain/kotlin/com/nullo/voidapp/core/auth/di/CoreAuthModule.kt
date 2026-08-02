package com.nullo.voidapp.core.auth.di

import com.nullo.voidapp.core.auth.data.repository.AuthRepositoryImpl
import com.nullo.voidapp.core.auth.data.service.AuthApiService
import com.nullo.voidapp.core.auth.data.service.AuthApiServiceImpl
import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreAuthModule = module {
    single<AuthApiService> { AuthApiServiceImpl(httpClient = get()) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            apiKeyStorage = get(),
            authApiService = get()
        )
    }

    factoryOf(::ValidateAndSaveApiKeyUseCase)
}
