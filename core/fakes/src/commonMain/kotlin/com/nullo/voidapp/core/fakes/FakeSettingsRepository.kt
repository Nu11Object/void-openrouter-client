package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initialTheme: AppTheme = AppTheme.SYSTEM) : SettingsRepository {

    private val currentTheme = MutableStateFlow(initialTheme)

    val calls: List<String>
        field = mutableListOf<String>()

    var setAppThemeFailure: Throwable? = null
    var clearFailure: Throwable? = null

    override val appTheme: Flow<AppTheme> = currentTheme

    override suspend fun setAppTheme(theme: AppTheme) {
        calls += "setAppTheme:${theme.name}"
        setAppThemeFailure?.let { throw it }
        currentTheme.update { theme }
    }

    override suspend fun clear() {
        calls += "clear"
        clearFailure?.let { throw it }
        currentTheme.update { AppTheme.SYSTEM }
    }

    fun changeThemeExternally(theme: AppTheme) {
        currentTheme.update { theme }
    }
}
