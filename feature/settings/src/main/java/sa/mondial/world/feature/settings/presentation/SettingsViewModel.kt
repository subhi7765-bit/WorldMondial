package sa.mondial.world.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.data.ThemePreference
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val localizationManager: LocalizationManager
) : ViewModel() {

    val notificationsEnabled = localizationManager.notificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val currentLanguage = localizationManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    val themePreference = localizationManager.themePreference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            localizationManager.setNotificationsEnabled(enabled)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            localizationManager.setLanguage(lang)
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            localizationManager.setThemePreference(theme)
        }
    }
}