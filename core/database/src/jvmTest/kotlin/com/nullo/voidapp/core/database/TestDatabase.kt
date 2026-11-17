package com.nullo.voidapp.core.database

import androidx.room3.Room

internal fun buildTestDatabase(): AppDatabase = getRoomDatabase(
    Room.inMemoryDatabaseBuilder<AppDatabase>(
        factory = AppDatabaseConstructor::initialize
    )
)
