package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher
import com.nullo.voidapp.feature.auth.presentation.AuthTabLauncherHolder
import com.nullo.voidapp.feature.auth.presentation.AuthTabResultRegistrar
import com.nullo.voidapp.feature.auth.presentation.DefaultAuthTabResultRegistrar
import com.nullo.voidapp.feature.auth.util.AndroidOAuthWebLauncher
import com.nullo.voidapp.feature.auth.util.AndroidPkceGenerator
import com.nullo.voidapp.feature.auth.util.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.util.util.PkceGenerator
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val authPlatformModule: Module = module {
    single { AuthTabLauncherHolder() }
    single { OAuthCallbackDispatcher() }
    single<AuthTabResultRegistrar> { DefaultAuthTabResultRegistrar(get(), get()) }
    single<PkceGenerator> { AndroidPkceGenerator }
    single<OAuthWebLauncher> { AndroidOAuthWebLauncher(get(), get()) }
}
