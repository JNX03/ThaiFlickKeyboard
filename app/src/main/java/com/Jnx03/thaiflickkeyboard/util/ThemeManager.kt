package com.Jnx03.thaiflickkeyboard.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager

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

    private val AMOLED = ThemeColors(
        kbBg = Color.parseColor("#000000"),
        charKeyBg = Color.parseColor("#1A1A1A"),
        charKeyPressed = Color.parseColor("#4285f4"),
        utilKeyBg = Color.parseColor("#0D0D0D"),
        utilKeyPressed = Color.parseColor("#1A1A1A"),
        textColor = Color.parseColor("#FFFFFF"),
        hintColor = Color.parseColor("#6E6E73"),
        flickBalloonBg = Color.parseColor("#1A1A1A"),
        flickBalloonActive = Color.parseColor("#4285f4"),
        dividerColor = Color.parseColor("#2C2C2E"),
        dimOverlay = Color.argb(180, 0, 0, 0)
    )

    var currentColors: ThemeColors = DARK
        private set

    // Appearance properties
    var cornerRadiusDp: Float = 10f
        private set
    var keySpacingDp: Float = 3f
        private set
    var fontSizeMultiplier: Float = 1.0f
        private set
    var keyBorderEnabled: Boolean = false
        private set
    var flickHintsVisible: Boolean = true
        private set
    var bottomPaddingDp: Int = 0
        private set
    var popupStyle: String = "balloon"
        private set

    fun init(context: Context, preference: String, accentColor: String = "#4285f4") {
        val base = when (preference) {
            "light" -> LIGHT
            "dark" -> DARK
            "amoled" -> AMOLED
            else -> if (isSystemDark(context)) DARK else LIGHT
        }
        val accent = try { Color.parseColor(accentColor) } catch (_: Exception) { Color.parseColor("#4285f4") }
        currentColors = base.copy(
            charKeyPressed = accent,
            flickBalloonActive = accent
        )
    }

    fun updateAppearance(prefs: PreferencesManager) {
        cornerRadiusDp = prefs.cornerRadius.toFloat()
        keySpacingDp = when (prefs.keySpacing) {
            "small" -> 2f
            "large" -> 5f
            else -> 3f
        }
        fontSizeMultiplier = when (prefs.fontSize) {
            "small" -> 0.85f
            "large" -> 1.15f
            else -> 1.0f
        }
        keyBorderEnabled = prefs.keyBorderEnabled
        flickHintsVisible = prefs.flickHintsVisible
        bottomPaddingDp = prefs.bottomPadding
        popupStyle = prefs.popupStyle
    }

    private fun isSystemDark(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
