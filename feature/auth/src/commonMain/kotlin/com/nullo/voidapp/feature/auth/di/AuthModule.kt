package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.data.network.service.AuthApiService
import com.nullo.voidapp.feature.auth.data.network.service.AuthApiServiceImpl
import com.nullo.voidapp.feature.auth.data.repository.AuthRepositoryImpl
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.auth.presentation.component.DefaultAuthComponent
import com.nullo.voidapp.feature.auth.presentation.store.AuthStoreFactory
import com.nullo.voidapp.feature.auth.util.repository.AuthRepository
import com.nullo.voidapp.feature.auth.util.usecase.ObserveAuthStateUseCase
import com.nullo.voidapp.feature.auth.util.usecase.SignInViaOpenRouterUseCase
import com.nullo.voidapp.feature.auth.util.usecase.SignInWithApiKeyUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal expect val authPlatformModule: Module

val authModule = module {
    includes(authPlatformModule)

    single<AuthApiService> { AuthApiServiceImpl(get()) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            apiKeyStorage = get(),
            authApiService = get(),
        )
    }

    factoryOf(::AuthStoreFactory)

    factory {
        AuthComponent.Factory { onAuthFinished, componentContext ->
            DefaultAuthComponent(
                componentContext = componentContext,
                onAuthFinish = onAuthFinished,
                authStoreFactory = get()
            )
        }
    }

    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::SignInWithApiKeyUseCase)
    factoryOf(::SignInViaOpenRouterUseCase)
}
