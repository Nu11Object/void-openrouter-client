package com.nullo.voidapp.core.security.di

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.security.IosApiKeyStorage
import org.koin.dsl.module

internal actual val platformSecurityModule = module {
    single<ApiKeyStorage> { IosApiKeyStorage() }
}
