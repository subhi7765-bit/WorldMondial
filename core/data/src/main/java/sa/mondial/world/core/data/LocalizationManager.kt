package sa.mondial.world.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference {
    DARK, LIGHT, SYSTEM
}

@Singleton
class LocalizationManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_LANG = stringPreferencesKey("app_language")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }

    /**
     * Emits stateful notifications activation flag. Defaults to true.
     */
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS] ?: true
    }

    /**
     * Updates notification setting inside central DataStore.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS] = enabled
        }
    }

    /**
     * Emits stateful current locale ("ar" or "en"). Defaults to Arabic ("ar").
     */
    val currentLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LANG] ?: "ar" 
    }

    /**
     * Saves localized intent state. Uses unidirectional flows instantly adjusting Compose configurations.
     */
    suspend fun setLanguage(lang: String) {
        require(lang == "ar" || lang == "en") { "Invalid language code" }
        dataStore.edit { preferences ->
            preferences[KEY_LANG] = lang
        }
    }

    /**
     * Emits the current ThemePreference. Defaults to SYSTEM.
     */
    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        when (preferences[KEY_THEME]) {
            "dark" -> ThemePreference.DARK
            "light" -> ThemePreference.LIGHT
            else -> ThemePreference.SYSTEM
        }
    }

    /**
     * Updates the user's ThemePreference.
     */
    suspend fun setThemePreference(theme: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = when (theme) {
                ThemePreference.DARK -> "dark"
                ThemePreference.LIGHT -> "light"
                ThemePreference.SYSTEM -> "system"
            }
        }
    }

    /**
     * Tracks Dark / Light Mode layout values (deprecated in favor of themePreference but kept for compat).
     */
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_THEME] == "dark"
    }

    suspend fun setThemeMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = if (isDark) "dark" else "light"
        }
    }
}