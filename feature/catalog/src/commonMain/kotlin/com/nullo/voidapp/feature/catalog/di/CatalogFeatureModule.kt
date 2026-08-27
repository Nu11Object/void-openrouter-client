package com.nullo.voidapp.feature.catalog.di

import com.nullo.voidapp.feature.catalog.data.CatalogRepositoryImpl
import com.nullo.voidapp.feature.catalog.domain.CatalogRepository
import com.nullo.voidapp.feature.catalog.domain.ObserveChatModelsUseCase
import com.nullo.voidapp.feature.catalog.domain.ObserveImageModelsUseCase
import com.nullo.voidapp.feature.catalog.domain.RefreshCatalogUseCase
import com.nullo.voidapp.feature.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.feature.catalog.domain.UseModelUseCase
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val catalogFeatureModule = module {
    singleOf(::CatalogRepositoryImpl) bind CatalogRepository::class
    factoryOf(::ObserveChatModelsUseCase)
    factoryOf(::ObserveImageModelsUseCase)
    factoryOf(::RefreshCatalogUseCase)
    factoryOf(::SetPinStatusUseCase)
    factoryOf(::UseModelUseCase)

    factoryOf(::CatalogStoreFactory)
}
