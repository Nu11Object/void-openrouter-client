package com.nullo.voidapp.core.datastore.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal class AndroidDataStoreFactory(
    private val context: Context
) : DataStoreFactory {

    override fun create(fileName: String): DataStore<Preferences> {
        return createDataStore {
            context.filesDir.resolve(fileName).absolutePath
        }
    }
}
