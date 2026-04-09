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

    var fontSize: String
        get() = prefs.getString(KEY_FONT_SIZE, "medium") ?: "medium"
        set(value) = prefs.edit().putString(KEY_FONT_SIZE, value).apply()

    var keySpacing: String
        get() = prefs.getString(KEY_KEY_SPACING, "medium") ?: "medium"
        set(value) = prefs.edit().putString(KEY_KEY_SPACING, value).apply()

    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, "#4285f4") ?: "#4285f4"
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    var keyBorderEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEY_BORDER, false)
        set(value) = prefs.edit().putBoolean(KEY_KEY_BORDER, value).apply()

    var cornerRadius: Int
        get() = prefs.getInt(KEY_CORNER_RADIUS, 10)
        set(value) = prefs.edit().putInt(KEY_CORNER_RADIUS, value).apply()

    var showToolbar: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TOOLBAR, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TOOLBAR, value).apply()

    var showSuggestions: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SUGGESTIONS, value).apply()

    var bottomPadding: Int
        get() = prefs.getInt(KEY_BOTTOM_PADDING, 0)
        set(value) = prefs.edit().putInt(KEY_BOTTOM_PADDING, value).apply()

    var flickHintsVisible: Boolean
        get() = prefs.getBoolean(KEY_FLICK_HINTS, true)
        set(value) = prefs.edit().putBoolean(KEY_FLICK_HINTS, value).apply()

    var popupStyle: String
        get() = prefs.getString(KEY_POPUP_STYLE, "balloon") ?: "balloon"
        set(value) = prefs.edit().putString(KEY_POPUP_STYLE, value).apply()

    companion object {
        private const val KEY_SENSITIVITY = "flick_sensitivity"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HEIGHT = "keyboard_height_percent"
        private const val KEY_PRESET = "selected_preset"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_KEY_SPACING = "key_spacing"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_KEY_BORDER = "key_border_enabled"
        private const val KEY_CORNER_RADIUS = "corner_radius"
        private const val KEY_SHOW_TOOLBAR = "show_toolbar"
        private const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        private const val KEY_BOTTOM_PADDING = "bottom_padding"
        private const val KEY_FLICK_HINTS = "flick_hints_visible"
        private const val KEY_POPUP_STYLE = "popup_style"
    }
}
