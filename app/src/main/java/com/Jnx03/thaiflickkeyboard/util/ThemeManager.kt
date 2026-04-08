package com.Jnx03.thaiflickkeyboard.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

data class ThemeColors(
    val kbBg: Int,
    val charKeyBg: Int,
    val charKeyPressed: Int,
    val utilKeyBg: Int,
    val utilKeyPressed: Int,
    val textColor: Int,
    val hintColor: Int,
    val flickBalloonBg: Int,
    val flickBalloonActive: Int,
    val dividerColor: Int,
    val dimOverlay: Int
)

object ThemeManager {

    private val DARK = ThemeColors(
        kbBg = Color.parseColor("#1C1C1E"),
        charKeyBg = Color.parseColor("#3A3A3C"),
        charKeyPressed = Color.parseColor("#4285f4"),
        utilKeyBg = Color.parseColor("#2C2C2E"),
        utilKeyPressed = Color.parseColor("#3A3A3C"),
        textColor = Color.parseColor("#FFFFFF"),
        hintColor = Color.parseColor("#8E8E93"),
        flickBalloonBg = Color.parseColor("#3A3A3C"),
        flickBalloonActive = Color.parseColor("#4285f4"),
        dividerColor = Color.parseColor("#3A3A3C"),
        dimOverlay = Color.argb(160, 0, 0, 0)
    )

    private val LIGHT = ThemeColors(
        kbBg = Color.parseColor("#D1D1D6"),
        charKeyBg = Color.parseColor("#FFFFFF"),
        charKeyPressed = Color.parseColor("#4285f4"),
        utilKeyBg = Color.parseColor("#AEB3BE"),
        utilKeyPressed = Color.parseColor("#C7C7CC"),
        textColor = Color.parseColor("#000000"),
        hintColor = Color.parseColor("#8E8E93"),
        flickBalloonBg = Color.parseColor("#FFFFFF"),
        flickBalloonActive = Color.parseColor("#4285f4"),
        dividerColor = Color.parseColor("#C7C7CC"),
        dimOverlay = Color.argb(80, 0, 0, 0)
    )

    var currentColors: ThemeColors = DARK
        private set

    fun init(context: Context, preference: String) {
        currentColors = when (preference) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> if (isSystemDark(context)) DARK else LIGHT
        }
    }

    private fun isSystemDark(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
