package com.nullo.voidapp.core.security.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.nullo.voidapp.core.security.AndroidApiKeyStorage
import com.nullo.voidapp.core.security.ApiKeyStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val securityModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = {
                androidContext().filesDir.resolve("api_key.preferences_pb")
            }
        )
    }
    single<ApiKeyStorage> {
        AndroidApiKeyStorage(
            context = androidContext(),
            dataStore = get()
        )
    }
}
