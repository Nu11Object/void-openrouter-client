package com.nullo.voidapp.feature.settings.domain

import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.designsystem.theme.AppTheme

/** Use case for managing and updating the application theme. */
internal class ChangeAppThemeUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(theme: AppTheme) {
        settingsRepository.setAppTheme(theme)
    }
}
