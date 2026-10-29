package com.nullo.voidapp.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.nullo.voidapp.core.desktop.DesktopAppDirs

internal fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = DesktopAppDirs.dataDir.resolve(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.toString()
    )
}
