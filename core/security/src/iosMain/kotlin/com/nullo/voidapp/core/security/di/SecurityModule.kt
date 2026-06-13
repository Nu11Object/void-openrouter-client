package com.nullo.voidapp.core.security.di

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.security.IosApiKeyStorage
import org.koin.dsl.module

actual val securityModule = module {
    single<ApiKeyStorage> { IosApiKeyStorage() }
}
