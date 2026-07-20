package com.nullo.voidapp.di

import com.nullo.voidapp.core.network.auth.di.networkAuthModule
import com.nullo.voidapp.core.network.client.networkClientModule
import com.nullo.voidapp.core.security.di.securityModule
import com.nullo.voidapp.feature.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            networkClientModule,
            networkAuthModule,
            securityModule,
            authModule,
        )
    }
}
