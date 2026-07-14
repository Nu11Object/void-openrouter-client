package com.nullo.voidapp.feature.auth.di

import com.nullo.voidapp.feature.auth.util.DesktopOAuthWebLauncher
import com.nullo.voidapp.feature.auth.util.JvmPkceGenerator
import com.nullo.voidapp.feature.auth.util.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.util.util.PkceGenerator
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val authPlatformModule: Module = module {
    single<PkceGenerator> { JvmPkceGenerator }
    single<OAuthWebLauncher> { DesktopOAuthWebLauncher() }
}
