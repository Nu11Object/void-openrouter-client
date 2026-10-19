package com.nullo.voidapp.core.data.catalog.di

import com.nullo.voidapp.core.data.catalog.data.CatalogRepositoryImpl
import com.nullo.voidapp.core.data.catalog.data.service.CatalogApiService
import com.nullo.voidapp.core.data.catalog.data.service.CatalogApiServiceImpl
import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.data.catalog.domain.ObserveChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObserveImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.RefreshCatalogUseCase
import com.nullo.voidapp.core.data.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.core.data.catalog.domain.UseModelUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val catalogModule = module {
    single<CatalogApiService> { CatalogApiServiceImpl(httpClient = get()) }

    singleOf(::CatalogRepositoryImpl) bind CatalogRepository::class

    factoryOf(::ObserveChatModelsUseCase)
    factoryOf(::ObserveImageModelsUseCase)
    factoryOf(::ObservePinnedChatModelsUseCase)
    factoryOf(::ObservePinnedImageModelsUseCase)
    factoryOf(::RefreshCatalogUseCase)
    factoryOf(::SetPinStatusUseCase)
    factoryOf(::UseModelUseCase)
}
