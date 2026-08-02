package com.nullo.voidapp.di

import com.nullo.voidapp.core.auth.di.coreAuthModule
import com.nullo.voidapp.core.datastore.di.dataStoreModule
import com.nullo.voidapp.core.network.client.networkClientModule
import com.nullo.voidapp.core.security.di.securityModule
import com.nullo.voidapp.core.data.settings.di.settingsModule
import com.nullo.voidapp.feature.auth.di.authFeatureModule
import com.nullo.voidapp.feature.root.di.rootModule
import com.nullo.voidapp.feature.settings.di.settingsFeatureModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            rootModule,
            networkClientModule,
            coreAuthModule,
            dataStoreModule,
            settingsModule,
            securityModule,
            authFeatureModule,
            settingsFeatureModule,
        )
    }
}
