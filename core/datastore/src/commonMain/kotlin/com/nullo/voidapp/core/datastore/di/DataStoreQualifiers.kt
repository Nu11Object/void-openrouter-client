package com.nullo.voidapp.core.datastore.di

import org.koin.core.qualifier.named

/** Koin qualifiers for DataStore instances. */
object DataStoreQualifiers {

    /** Qualifier for the application settings DataStore. */
    val settings = named("datastore_settings")

    /** Qualifier for the encrypted API key DataStore. */
    val apiKeyStorage = named("datastore_api_storage")
}
