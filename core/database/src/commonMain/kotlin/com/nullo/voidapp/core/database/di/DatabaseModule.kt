package com.nullo.voidapp.core.database.di

import com.nullo.voidapp.core.database.AppDatabase
import com.nullo.voidapp.core.database.dao.ChatModelDao
import com.nullo.voidapp.core.database.dao.ConversationDao
import com.nullo.voidapp.core.database.dao.ImageModelDao
import com.nullo.voidapp.core.database.dao.MessageDao
import com.nullo.voidapp.core.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformDatabaseModule: Module

val databaseModule = module {
    includes(platformDatabaseModule)

    single<AppDatabase> { getRoomDatabase(builder = get()) }
    single<ChatModelDao> { get<AppDatabase>().aiModelDao() }
    single<ImageModelDao> { get<AppDatabase>().imageModelDao() }
    single<ConversationDao> { get<AppDatabase>().conversationDao() }
    single<MessageDao> { get<AppDatabase>().messageDao() }
}
