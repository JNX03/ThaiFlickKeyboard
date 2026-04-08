package com.Jnx03.thaiflickkeyboard.data

import android.content.Context
import android.content.SharedPreferences
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.google.gson.Gson

class LayoutRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveLayout(layout: KeyboardLayout) {
        prefs.edit().putString(KEY_LAYOUT, gson.toJson(layout)).apply()
    }

    fun loadLayout(): KeyboardLayout {
        val json = prefs.getString(KEY_LAYOUT, null)
        return if (json != null) {
            try {
                gson.fromJson(json, KeyboardLayout::class.java)
            } catch (e: Exception) {
                loadPresetLayout()
            }
        } else {
            loadPresetLayout()
        }
    }

    fun loadPresetLayout(): KeyboardLayout {
        val presetName = prefs.getString(KEY_PRESET, null)
        return if (presetName != null) {
            KeyboardLayout.fromPresetName(presetName)
        } else {
            KeyboardLayout.padPimOpti()
        }
    }

    fun setPreset(presetName: String) {
        prefs.edit()
            .putString(KEY_PRESET, presetName)
            .remove(KEY_LAYOUT)
            .apply()
    }

    fun resetToDefault() {
        prefs.edit().remove(KEY_LAYOUT).remove(KEY_PRESET).apply()
    }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val KEY_LAYOUT = "custom_keyboard_layout"
        private const val KEY_PRESET = "selected_preset"
    }
}
