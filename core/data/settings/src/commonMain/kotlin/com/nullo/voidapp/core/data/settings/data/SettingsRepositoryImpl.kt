package com.nullo.voidapp.core.data.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    override val appTheme: Flow<AppTheme>
        get() = dataStore.data.map { prefs ->
            val theme = prefs[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
            runCatching { AppTheme.valueOf(theme) }.getOrDefault(AppTheme.SYSTEM)
        }

    override suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_THEME] = theme.name
        }
    }
}
