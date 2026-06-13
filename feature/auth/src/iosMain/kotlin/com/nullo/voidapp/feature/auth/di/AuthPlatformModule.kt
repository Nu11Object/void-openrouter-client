package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.domain.IosOAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.IosPkceGenerator
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val authPlatformModule: Module = module {
    single<PkceGenerator> { IosPkceGenerator }
    single<OAuthWebLauncher> { IosOAuthWebLauncher() }
}
