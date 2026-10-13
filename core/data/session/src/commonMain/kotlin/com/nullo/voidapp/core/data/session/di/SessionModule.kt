package com.nullo.voidapp.core.data.session.di

import com.nullo.voidapp.core.data.session.data.SessionRepositoryImpl
import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.datastore.data.DataStoreFactory
import com.nullo.voidapp.core.datastore.di.DataStoreQualifiers
import org.koin.dsl.module

val sessionModule = module {

    single(qualifier = DataStoreQualifiers.session) {
        get<DataStoreFactory>().create(
            fileName = "session"
        )
    }

    single<SessionRepository> {
        SessionRepositoryImpl(
            dataStore = get(DataStoreQualifiers.session),
            json = get()
        )
    }
}
