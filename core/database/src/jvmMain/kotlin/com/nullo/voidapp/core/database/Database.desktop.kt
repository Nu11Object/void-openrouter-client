package com.nullo.voidapp.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

internal fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}
