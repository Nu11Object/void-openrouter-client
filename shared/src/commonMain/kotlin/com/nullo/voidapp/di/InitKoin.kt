package com.nullo.voidapp.di

import com.nullo.voidapp.core.auth.di.coreAuthModule
import com.nullo.voidapp.core.data.catalog.di.catalogModule
import com.nullo.voidapp.core.data.chat.di.chatModule
import com.nullo.voidapp.core.data.session.di.sessionModule
import com.nullo.voidapp.core.data.settings.di.settingsModule
import com.nullo.voidapp.core.database.di.databaseModule
import com.nullo.voidapp.core.datastore.di.dataStoreModule
import com.nullo.voidapp.core.network.networkModule
import com.nullo.voidapp.core.security.di.securityModule
import com.nullo.voidapp.feature.auth.di.authFeatureModule
import com.nullo.voidapp.feature.catalog.di.catalogFeatureModule
import com.nullo.voidapp.feature.chat.di.chatFeatureModule
import com.nullo.voidapp.feature.root.di.rootModule
import com.nullo.voidapp.feature.settings.di.settingsFeatureModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            rootModule,
            networkModule,
            coreAuthModule,
            dataStoreModule,
            databaseModule,
            settingsModule,
            sessionModule,
            catalogModule,
            chatModule,
            securityModule,
            authFeatureModule,
            settingsFeatureModule,
            catalogFeatureModule,
            chatFeatureModule,
        )
    }
}
