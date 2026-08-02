package com.nullo.voidapp.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nullo.voidapp.core.datastore.di.DataStoreQualifiers

/** Creates platform-specific DataStore instances. */
interface DataStoreFactory {

    /**
     * Creates a DataStore backed by the specified file.
     *
     * To register or resolve a specific instance, use [DataStoreQualifiers].
     *
     * Example:
     * ```kotlin
     * // Register in DI module with qualifier:
     * single(DataStoreQualifiers.settings) {
     *     get<DataStoreFactory>().create("settings")
     * }
     * ```
     *
     * @param fileName The file name without extension.
     */
    fun create(fileName: String): DataStore<Preferences>
}
