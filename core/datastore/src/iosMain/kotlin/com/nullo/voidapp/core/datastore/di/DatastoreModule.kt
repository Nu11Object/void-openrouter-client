package com.nullo.voidapp.core.datastore.di

import com.nullo.voidapp.core.datastore.data.DataStoreFactory
import com.nullo.voidapp.core.datastore.data.IosDataStoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreModule: Module = module {
    single<DataStoreFactory> { IosDataStoreFactory() }
}
