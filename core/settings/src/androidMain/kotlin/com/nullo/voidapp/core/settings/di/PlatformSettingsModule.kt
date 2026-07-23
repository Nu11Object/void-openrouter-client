package com.nullo.voidapp.core.settings.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nullo.voidapp.core.settings.data.createAndroidDataStore
import org.koin.dsl.module

internal actual val platformSettingsModule = module {
    single<DataStore<Preferences>> { createAndroidDataStore(get()) }
}
