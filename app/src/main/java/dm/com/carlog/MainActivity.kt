package dm.com.carlog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dm.com.carlog.model.SettingsViewModel
import dm.com.carlog.model.ThemeMode
import dm.com.carlog.ui.CarLog
import dm.com.carlog.ui.theme.CarLogTheme
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isRestarting = false
    private var currentLanguage = "en"

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString("language", "en") ?: "en"
        currentLanguage = savedLanguage

        val context = updateLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        currentLanguage = sharedPrefs.getString("language", "en") ?: "en"

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            var languageChanged by remember { mutableStateOf(false) }

            LaunchedEffect(settingsState.language) {
                if (settingsState.language != currentLanguage) {
                    // Ждем 1 секунду перед перезапуском
                    kotlinx.coroutines.delay(1000)
                    saveLanguage(settingsState.language)
                    restartActivity()
                }
            }

            CarLogTheme(
                darkTheme = when (settingsState.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                CarLog()
            }
        }
    }

    private fun saveLanguage(language: String) {
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("language", language)
            .apply()
        currentLanguage = language
    }

    private fun restartActivity() {
        if (isRestarting) return
        isRestarting = true

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}