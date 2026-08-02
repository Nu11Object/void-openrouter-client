package com.nullo.voidapp.core.security.di

import com.nullo.voidapp.core.datastore.di.DataStoreQualifiers
import com.nullo.voidapp.core.security.AndroidApiKeyStorage
import com.nullo.voidapp.core.security.ApiKeyStorage
import org.koin.dsl.module

internal actual val platformSecurityModule = module {

    single<ApiKeyStorage> {
        AndroidApiKeyStorage(
            context = get(),
            dataStore = get(DataStoreQualifiers.apiKeyStorage)
        )
    }
}
