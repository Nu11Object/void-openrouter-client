package com.nullo.voidapp.feature.settings.domain

import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Use case for observing the application theme. */
internal class ObserveAppThemeUseCase(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<AppTheme> {
        return settingsRepository.appTheme.distinctUntilChanged()
    }
}
