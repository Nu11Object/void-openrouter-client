package com.nullo.voidapp.core.datastore.di

import com.nullo.voidapp.core.datastore.data.AndroidDataStoreFactory
import com.nullo.voidapp.core.datastore.data.DataStoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreModule: Module = module {
    single<DataStoreFactory> { AndroidDataStoreFactory(context = get()) }
}
