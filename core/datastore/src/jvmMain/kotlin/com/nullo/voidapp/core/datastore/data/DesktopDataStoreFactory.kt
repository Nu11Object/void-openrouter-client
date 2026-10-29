package com.nullo.voidapp.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nullo.voidapp.core.desktop.DesktopAppDirs

internal class DesktopDataStoreFactory : DataStoreFactory {

    override fun create(fileName: String): DataStore<Preferences> = createDataStore {
        DesktopAppDirs.dataDir.resolve(fileName).toString()
    }
}
