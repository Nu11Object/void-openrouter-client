package com.nullo.voidapp.feature.root.di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.feature.root.component.DefaultRootComponent
import com.nullo.voidapp.feature.root.component.RootComponent
import org.koin.dsl.module

val rootModule = module {
    single<StoreFactory> { DefaultStoreFactory() }

    factory<RootComponent> { params ->
        DefaultRootComponent(
            componentContext = params.get<ComponentContext>(),
            authComponentFactory = get(),
            settingsComponentFactory = get(),
            settingsRepository = get(),
            apiKeyStorage = get(),
        )
    }
}
