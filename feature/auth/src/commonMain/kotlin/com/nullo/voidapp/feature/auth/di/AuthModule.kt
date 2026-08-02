package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInViaOpenRouterUseCase
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.auth.presentation.component.DefaultAuthComponent
import com.nullo.voidapp.feature.auth.presentation.store.AuthStoreFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal expect val authPlatformModule: Module

val authFeatureModule = module {
    includes(authPlatformModule)

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

    factoryOf(::SignInViaOpenRouterUseCase)
    factoryOf(::ObserveAuthStateUseCase)
}
