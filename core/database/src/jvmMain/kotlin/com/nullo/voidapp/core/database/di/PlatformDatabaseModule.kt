package com.nullo.voidapp.core.database.di

import com.nullo.voidapp.core.database.getDatabaseBuilder
import org.koin.dsl.module

internal actual val platformDatabaseModule = module {
    single { getDatabaseBuilder() }
}
