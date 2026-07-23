package com.nullo.voidapp.core.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

internal fun createDesktopDataStore(): DataStore<Preferences> = createDataStore {
    val appDir = File(System.getProperty("user.home"), ".voidapp")
    if (!appDir.exists()) appDir.mkdirs()
    File(appDir, DATASTORE_SETTINGS_FILENAME).absolutePath
}
