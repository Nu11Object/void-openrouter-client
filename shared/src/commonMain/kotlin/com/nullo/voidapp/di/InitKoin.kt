package com.nullo.voidapp.di

import com.nullo.voidapp.core.network.di.networkModule
import com.nullo.voidapp.core.security.di.securityModule
import com.nullo.voidapp.feature.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            networkModule,
            securityModule,
            authModule,
        )
    }
}
