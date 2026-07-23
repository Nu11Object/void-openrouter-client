package com.nullo.voidapp.core.settings.di

import com.nullo.voidapp.core.settings.data.SettingsRepositoryImpl
import com.nullo.voidapp.core.settings.domain.SettingsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformSettingsModule: Module

val settingsModule = module {
    includes(platformSettingsModule)

    single<SettingsRepository> {
        SettingsRepositoryImpl(dataStore = get())
    }
}
