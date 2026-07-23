package com.nullo.voidapp.core.settings.data

import android.content.Context

internal fun createAndroidDataStore(context: Context) = createDataStore {
    context.filesDir.resolve(DATASTORE_SETTINGS_FILENAME).absolutePath
}
