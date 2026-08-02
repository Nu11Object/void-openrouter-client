package com.nullo.voidapp.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

private const val SUFFIX_PREFERENCES_PB = ".preferences_pb"

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().withPreferencesPbSuffix().toPath() }
    )
}

private fun String.withPreferencesPbSuffix(): String =
    "${removeSuffix(SUFFIX_PREFERENCES_PB)}$SUFFIX_PREFERENCES_PB"
