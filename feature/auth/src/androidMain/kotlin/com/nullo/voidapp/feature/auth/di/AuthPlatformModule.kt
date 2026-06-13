package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher
import com.nullo.voidapp.feature.auth.domain.AndroidOAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.AndroidPkceGenerator
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator
import com.nullo.voidapp.feature.auth.presentation.AuthTabLauncherHolder
import com.nullo.voidapp.feature.auth.presentation.AuthTabResultRegistrar
import com.nullo.voidapp.feature.auth.presentation.DefaultAuthTabResultRegistrar
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val authPlatformModule: Module = module {
    single { AuthTabLauncherHolder() }
    single { OAuthCallbackDispatcher() }
    single<AuthTabResultRegistrar> { DefaultAuthTabResultRegistrar(get(), get()) }
    single<PkceGenerator> { AndroidPkceGenerator }
    single<OAuthWebLauncher> { AndroidOAuthWebLauncher(get(), get()) }
}
