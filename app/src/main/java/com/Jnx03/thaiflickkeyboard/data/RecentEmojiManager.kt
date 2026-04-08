package com.Jnx03.thaiflickkeyboard.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentEmojiManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("recent_emojis", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addRecent(emoji: String) {
        if (emoji.isBlank()) return
        val list = getRecent().toMutableList()
        list.removeAll { it == emoji }
        list.add(0, emoji)
        while (list.size > MAX_RECENT) list.removeAt(list.size - 1)
        prefs.edit().putString(KEY_RECENT, gson.toJson(list)).apply()
    }

    fun getRecent(): List<String> {
        val json = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun clear() {
        prefs.edit().remove(KEY_RECENT).apply()
    }

    companion object {
        private const val KEY_RECENT = "recent"
        private const val MAX_RECENT = 30
    }
}
