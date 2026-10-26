package com.nullo.voidapp.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.nullo.voidapp.core.database.dao.ChatModelDao
import com.nullo.voidapp.core.database.dao.ConversationDao
import com.nullo.voidapp.core.database.dao.ImageModelDao
import com.nullo.voidapp.core.database.dao.MessageDao
import com.nullo.voidapp.core.database.entity.ChatModelEntity
import com.nullo.voidapp.core.database.entity.ConversationEntity
import com.nullo.voidapp.core.database.entity.ImageModelEntity
import com.nullo.voidapp.core.database.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DATABASE_NAME = "void_app.db"
private const val DATABASE_VERSION = 4

@Database(
    entities = [
        ChatModelEntity::class,
        ImageModelEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = DATABASE_VERSION
)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun aiModelDao(): ChatModelDao
    abstract fun imageModelDao(): ImageModelDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {

    override fun initialize(): AppDatabase
}

internal fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .fallbackToDestructiveMigration(dropAllTables = true) // todo: remove before release
    .build()
