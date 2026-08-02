package com.nullo.voidapp.core.security.di

import com.nullo.voidapp.core.datastore.data.DataStoreFactory
import com.nullo.voidapp.core.datastore.di.DataStoreQualifiers
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformSecurityModule: Module

val securityModule: Module = module {
    includes(platformSecurityModule)

    single(qualifier = DataStoreQualifiers.apiKeyStorage) {
        get<DataStoreFactory>().create(
            fileName = "api_key"
        )
    }
}
