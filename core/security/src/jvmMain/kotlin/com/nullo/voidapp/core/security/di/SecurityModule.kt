package com.nullo.voidapp.core.security.di

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.security.DesktopApiKeyStorage
import org.koin.dsl.module

actual val securityModule = module {
    single<ApiKeyStorage> { DesktopApiKeyStorage() }
}
