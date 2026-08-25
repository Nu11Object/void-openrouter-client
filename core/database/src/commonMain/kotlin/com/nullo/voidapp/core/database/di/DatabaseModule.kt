package com.nullo.voidapp.core.database.di

import com.nullo.voidapp.core.database.AppDatabase
import com.nullo.voidapp.core.database.dao.AiModelDao
import com.nullo.voidapp.core.database.dao.ImageModelDao
import com.nullo.voidapp.core.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformDatabaseModule: Module

val databaseModule = module {
    includes(platformDatabaseModule)

    single<AppDatabase> { getRoomDatabase(builder = get()) }
    single<AiModelDao> { get<AppDatabase>().aiModelDao() }
    single<ImageModelDao> { get<AppDatabase>().imageModelDao() }
}
