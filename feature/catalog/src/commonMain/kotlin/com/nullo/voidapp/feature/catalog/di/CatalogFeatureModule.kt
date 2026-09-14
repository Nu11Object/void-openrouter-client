package com.nullo.voidapp.feature.catalog.di

import com.nullo.voidapp.feature.catalog.presentation.component.CatalogComponent
import com.nullo.voidapp.feature.catalog.presentation.component.DefaultCatalogComponent
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val catalogFeatureModule = module {
    factoryOf(::CatalogStoreFactory)

    factory {
        CatalogComponent.Factory { onNavigateBack, componentContext ->
            DefaultCatalogComponent(
                onNavigateBack = onNavigateBack,
                catalogStoreFactory = get(),
                componentContext = componentContext,
            )
        }
    }
}
