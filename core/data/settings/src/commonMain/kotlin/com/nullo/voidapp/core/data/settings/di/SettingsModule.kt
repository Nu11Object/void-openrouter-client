package com.nullo.voidapp.core.data.settings.di

import com.nullo.voidapp.core.data.settings.data.SettingsRepositoryImpl
import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.datastore.data.DataStoreFactory
import com.nullo.voidapp.core.datastore.di.DataStoreQualifiers
import org.koin.dsl.module

val settingsModule = module {

    single(qualifier = DataStoreQualifiers.settings) {
        get<DataStoreFactory>().create(
            fileName = "settings"
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            dataStore = get(DataStoreQualifiers.settings)
        )
    }
}
