package com.nullo.voidapp.core.data.chat.di

import com.nullo.voidapp.core.data.chat.data.repository.ChatRepositoryImpl
import com.nullo.voidapp.core.data.chat.data.repository.ConversationRepositoryImpl
import com.nullo.voidapp.core.data.chat.data.ResponseGeneratorImpl
import com.nullo.voidapp.core.data.chat.data.service.ChatApiService
import com.nullo.voidapp.core.data.chat.data.service.ChatApiServiceImpl
import com.nullo.voidapp.core.data.chat.data.storage.FileKitImageEncoder
import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.data.chat.domain.ResponseGenerator
import com.nullo.voidapp.core.data.chat.domain.ImageEncoder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatModule = module {
    single<ChatApiService> {
        ChatApiServiceImpl(
            httpClient = get(),
            json = get()
        )
    }

    single<ImageEncoder> { FileKitImageEncoder() }

    single<ResponseGenerator> {
        ResponseGeneratorImpl(
            chatApiService = get(),
            sessionRepository = get(),
            imageEncoder = get()
        )
    }

    singleOf(::ChatRepositoryImpl) bind ChatRepository::class
    singleOf(::ConversationRepositoryImpl) bind ConversationRepository::class
}
