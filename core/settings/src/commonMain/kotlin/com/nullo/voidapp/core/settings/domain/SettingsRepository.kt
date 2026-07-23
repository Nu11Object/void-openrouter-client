package com.nullo.voidapp.core.settings.domain

import com.nullo.voidapp.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.Flow

/** Repository for managing user preferences and application settings. */
interface SettingsRepository {

    /**
     * A flow of the currently selected [AppTheme].
     *
     * Defaults to [AppTheme.SYSTEM] if no preference is stored or if the value is invalid.
     */
    val appTheme: Flow<AppTheme>

    /** Updates and persists the user's preferred [AppTheme]. */
    suspend fun setAppTheme(theme: AppTheme)
}
