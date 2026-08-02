package com.nullo.voidapp.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

internal class DesktopDataStoreFactory : DataStoreFactory {

    override fun create(fileName: String): DataStore<Preferences> {
        return createDataStore {
            val appDir = File(System.getProperty("user.home"), ".voidapp")
            if (!appDir.exists()) appDir.mkdirs()
            File(appDir, fileName).absolutePath
        }
    }
}
