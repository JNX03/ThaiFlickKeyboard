package com.Jnx03.thaiflickkeyboard.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ClipboardHistoryManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("clipboard_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    data class ClipEntry(val text: String, val timestamp: Long)

    fun addClip(text: String) {
        if (text.isBlank()) return
        val history = getHistory().toMutableList()
        // Remove duplicate if exists
        history.removeAll { it.text == text }
        // Add to front
        history.add(0, ClipEntry(text, System.currentTimeMillis()))
        // Keep max 20
        while (history.size > MAX_ENTRIES) history.removeAt(history.size - 1)
        save(history)
    }

    fun getHistory(): List<ClipEntry> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ClipEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun removeClip(index: Int) {
        val history = getHistory().toMutableList()
        if (index in history.indices) {
            history.removeAt(index)
            save(history)
        }
    }

    fun clearAll() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(history: List<ClipEntry>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply()
    }

    companion object {
        private const val KEY_HISTORY = "history"
        private const val MAX_ENTRIES = 20
    }
}
