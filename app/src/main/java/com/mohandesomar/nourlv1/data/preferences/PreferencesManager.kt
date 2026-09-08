package com.mohandesomar.nourlv1.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mohandesomar.nourlv1.sound.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class AppSettings(
    val darkTheme: Boolean = true,
    val fontScale: Float = 1.0f, // 0.85f = Small, 1.0f = Medium, 1.15f = Large
    val language: String = "ar", // "ar" or "en"
    val soundEnabled: Boolean = true
)

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    init {
        // Synchronize initial sound manager state
        SoundManager.isSoundEnabled = _settingsFlow.value.soundEnabled
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
            fontScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f),
            language = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar",
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        )
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(darkTheme = enabled)
    }

    fun setFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
        _settingsFlow.value = _settingsFlow.value.copy(fontScale = scale)
    }

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
        _settingsFlow.value = _settingsFlow.value.copy(language = language)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        SoundManager.isSoundEnabled = enabled
        _settingsFlow.value = _settingsFlow.value.copy(soundEnabled = enabled)
    }

    fun getUserInviteCode(): String {
        var code = prefs.getString(KEY_USER_INVITE_CODE, null)
        if (code.isNullOrBlank()) {
            code = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
            prefs.edit().putString(KEY_USER_INVITE_CODE, code).apply()
        }
        return code
    }

    fun getFriendInviteCodes(): Set<String> {
        return prefs.getStringSet(KEY_FRIEND_INVITE_CODES, emptySet()) ?: emptySet()
    }

    fun addFriendInviteCode(code: String) {
        val current = getFriendInviteCodes().toMutableSet()
        current.add(code.trim().uppercase())
        prefs.edit().putStringSet(KEY_FRIEND_INVITE_CODES, current).apply()
    }

    fun removeFriendInviteCode(code: String) {
        val current = getFriendInviteCodes().toMutableSet()
        current.remove(code.trim().uppercase())
        prefs.edit().putStringSet(KEY_FRIEND_INVITE_CODES, current).apply()
    }

    companion object {
        private const val PREFS_NAME = "nour_app_preferences"
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_FONT_SCALE = "key_font_scale"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_USER_INVITE_CODE = "key_user_invite_code"
        private const val KEY_FRIEND_INVITE_CODES = "key_friend_invite_codes"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
