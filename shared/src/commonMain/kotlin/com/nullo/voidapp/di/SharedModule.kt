package com.nullo.voidapp.di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.component.DefaultRootComponent
import com.nullo.voidapp.component.RootComponent
import org.koin.dsl.module

val sharedModule = module {
    single<StoreFactory> { DefaultStoreFactory() }

    factory<RootComponent> { params ->
        DefaultRootComponent(
            componentContext = params.get<ComponentContext>(),
            authComponentFactory = get(),
            settingsRepository = get(),
        )
    }
}
