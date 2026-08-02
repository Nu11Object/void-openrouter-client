package com.nullo.voidapp.feature.settings.di

import com.nullo.voidapp.feature.settings.domain.ChangeAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.ObserveAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.SignOutUseCase
import com.nullo.voidapp.feature.settings.presentation.component.DefaultSettingsComponent
import com.nullo.voidapp.feature.settings.presentation.component.SettingsComponent
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    factoryOf(::SettingsStoreFactory)

    factory {
        SettingsComponent.Factory { onNavigateBack, onSignedOut, componentContext ->
            DefaultSettingsComponent(
                onNavigateBack = onNavigateBack,
                onSignedOut = onSignedOut,
                settingsStoreFactory = get(),
                componentContext = componentContext,
            )
        }
    }

    factoryOf(::ChangeAppThemeUseCase)
    factoryOf(::ObserveAppThemeUseCase)
    factoryOf(::SignOutUseCase)
}
