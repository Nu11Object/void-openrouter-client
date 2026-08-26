package com.nullo.voidapp.core.data.catalog.di

import com.nullo.voidapp.core.data.catalog.service.CatalogApiService
import com.nullo.voidapp.core.data.catalog.service.CatalogApiServiceImpl
import org.koin.dsl.module

val catalogModule = module {
    single<CatalogApiService> { CatalogApiServiceImpl(httpClient = get()) }
}
