package com.Jnx03.thaiflickkeyboard.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var flickSensitivity: Float
        get() = prefs.getFloat(KEY_SENSITIVITY, 20f)
        set(value) = prefs.edit().putFloat(KEY_SENSITIVITY, value).apply()

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var keyboardHeightPercent: Int
        get() = prefs.getInt(KEY_HEIGHT, 38)
        set(value) = prefs.edit().putInt(KEY_HEIGHT, value).apply()

    var selectedPreset: String
        get() = prefs.getString(KEY_PRESET, "PadPim - Opti") ?: "PadPim - Opti"
        set(value) = prefs.edit().putString(KEY_PRESET, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "dark") ?: "dark"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var tutorialSeen: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL_SEEN, value).apply()

    companion object {
        private const val KEY_SENSITIVITY = "flick_sensitivity"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HEIGHT = "keyboard_height_percent"
        private const val KEY_PRESET = "selected_preset"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
    }
}
