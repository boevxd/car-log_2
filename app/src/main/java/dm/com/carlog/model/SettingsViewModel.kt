package dm.com.carlog.model

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data
                .map { preferences ->
                    val themeModeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
                    val language = preferences[PreferencesKeys.LANGUAGE] ?: "en"

                    SettingsState(
                        themeMode = ThemeMode.valueOf(themeModeName),
                        language = language
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            if (themeMode != _uiState.value.themeMode) {
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.THEME_MODE] = themeMode.name
                }
            }
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            if (language != _uiState.value.language) {
                try {
                    dataStore.edit { preferences ->
                        preferences[PreferencesKeys.LANGUAGE] = language
                    }

                    _uiState.value = _uiState.value.copy(language = language)

                    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    sharedPrefs.edit()
                        .putString("language", language)
                        .apply()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "en",
    val isProcessing: Boolean = false
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

private object PreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LANGUAGE = stringPreferencesKey("app_language")
}