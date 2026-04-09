package com.Jnx03.thaiflickkeyboard.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var layoutRepository: LayoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        prefsManager = PreferencesManager(this)
        applyThemeMode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        layoutRepository = LayoutRepository(this)
        ThemeManager.init(this, prefsManager.themeMode, prefsManager.accentColor)

        // Enable keyboard
        findViewById<LinearLayout>(R.id.row_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // Select keyboard
        findViewById<LinearLayout>(R.id.row_select).setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // Theme picker
        findViewById<LinearLayout>(R.id.row_theme).setOnClickListener {
            showThemePicker()
        }
        updateThemeDisplay()

        // Accent Color
        findViewById<LinearLayout>(R.id.row_accent_color).setOnClickListener {
            showAccentColorPicker()
        }
        updateAccentColorDisplay()

        // Layout preset
        findViewById<LinearLayout>(R.id.row_preset).setOnClickListener {
            showPresetPicker()
        }
        updatePresetDisplay()

        // Flick sensitivity slider
        val sliderSensitivity = findViewById<Slider>(R.id.slider_sensitivity)
        val tvSensitivity = findViewById<TextView>(R.id.tv_sensitivity_value)
        sliderSensitivity.value = prefsManager.flickSensitivity
        tvSensitivity.text = prefsManager.flickSensitivity.toInt().toString()
        sliderSensitivity.addOnChangeListener { _, value, _ ->
            prefsManager.flickSensitivity = value
            tvSensitivity.text = value.toInt().toString()
        }

        // Keyboard height slider
        val sliderHeight = findViewById<Slider>(R.id.slider_height)
        val tvHeight = findViewById<TextView>(R.id.tv_height_value)
        sliderHeight.value = prefsManager.keyboardHeightPercent.toFloat()
        tvHeight.text = "${prefsManager.keyboardHeightPercent}%"
        sliderHeight.addOnChangeListener { _, value, _ ->
            prefsManager.keyboardHeightPercent = value.toInt()
            tvHeight.text = "${value.toInt()}%"
        }

        // Font Size
        findViewById<LinearLayout>(R.id.row_font_size).setOnClickListener {
            showFontSizePicker()
        }
        updateFontSizeDisplay()

        // Key Spacing
        findViewById<LinearLayout>(R.id.row_key_spacing).setOnClickListener {
            showKeySpacingPicker()
        }
        updateKeySpacingDisplay()

        // Corner Radius slider
        val sliderCorner = findViewById<Slider>(R.id.slider_corner_radius)
        val tvCorner = findViewById<TextView>(R.id.tv_corner_radius_value)
        sliderCorner.value = prefsManager.cornerRadius.toFloat()
        tvCorner.text = prefsManager.cornerRadius.toString()
        sliderCorner.addOnChangeListener { _, value, _ ->
            prefsManager.cornerRadius = value.toInt()
            tvCorner.text = value.toInt().toString()
        }

        // Bottom Padding
        findViewById<LinearLayout>(R.id.row_bottom_padding).setOnClickListener {
            showBottomPaddingPicker()
        }
        updateBottomPaddingDisplay()

        // Key Border toggle
        val switchBorder = findViewById<SwitchMaterial>(R.id.switch_key_border)
        switchBorder.isChecked = prefsManager.keyBorderEnabled
        switchBorder.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.keyBorderEnabled = isChecked
        }

        // Flick Hints toggle
        val switchHints = findViewById<SwitchMaterial>(R.id.switch_flick_hints)
        switchHints.isChecked = prefsManager.flickHintsVisible
        switchHints.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.flickHintsVisible = isChecked
        }

        // Popup Style
        findViewById<LinearLayout>(R.id.row_popup_style).setOnClickListener {
            showPopupStylePicker()
        }
        updatePopupStyleDisplay()

        // Show Toolbar toggle
        val switchToolbar = findViewById<SwitchMaterial>(R.id.switch_show_toolbar)
        switchToolbar.isChecked = prefsManager.showToolbar
        switchToolbar.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showToolbar = isChecked
        }

        // Show Suggestions toggle
        val switchSuggestions = findViewById<SwitchMaterial>(R.id.switch_show_suggestions)
        switchSuggestions.isChecked = prefsManager.showSuggestions
        switchSuggestions.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showSuggestions = isChecked
        }

        // Haptic toggle
        val switchHaptic = findViewById<SwitchMaterial>(R.id.switch_haptic)
        switchHaptic.isChecked = prefsManager.hapticEnabled
        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.hapticEnabled = isChecked
        }

        // Sound toggle
        val switchSound = findViewById<SwitchMaterial>(R.id.switch_sound)
        switchSound.isChecked = prefsManager.soundEnabled
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.soundEnabled = isChecked
        }

        // Tutorial
        findViewById<LinearLayout>(R.id.row_tutorial).setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        // Practice
        findViewById<LinearLayout>(R.id.row_practice).setOnClickListener {
            startActivity(Intent(this, PracticeActivity::class.java))
        }

        // About
        findViewById<LinearLayout>(R.id.row_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        requestMicPermissionIfNeeded()

        // Auto-show tutorial on first install
        if (!prefsManager.tutorialSeen) {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
    }

    private fun applyThemeMode() {
        val mode = PreferencesManager(this).themeMode
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark", "amoled" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun showThemePicker() {
        val options = arrayOf("Light", "Dark", "AMOLED Black", "Follow System")
        val values = arrayOf("light", "dark", "amoled", "system")
        val current = values.indexOf(prefsManager.themeMode).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Theme")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefsManager.themeMode = values[which]
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateThemeDisplay() {
        val label = when (prefsManager.themeMode) {
            "light" -> "Light"
            "dark" -> "Dark"
            "amoled" -> "AMOLED Black"
            else -> "Follow System"
        }
        findViewById<TextView>(R.id.tv_theme_value).text = label
    }

    // ── Accent Color ──

    private val accentColors = arrayOf(
        Pair("Blue", "#4285f4"),
        Pair("Red", "#EA4335"),
        Pair("Green", "#34A853"),
        Pair("Purple", "#A855F7"),
        Pair("Orange", "#F59E0B"),
        Pair("Pink", "#EC4899"),
        Pair("Teal", "#03DAC5")
    )

    private fun showAccentColorPicker() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }

        val sizePx = (40 * resources.displayMetrics.density).toInt()
        val marginPx = (8 * resources.displayMetrics.density).toInt()

        val dialog = AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Accent Color")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .create()

        for ((name, hex) in accentColors) {
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(marginPx, 0, marginPx, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    if (prefsManager.accentColor == hex) {
                        setStroke((3 * resources.displayMetrics.density).toInt(), Color.WHITE)
                    }
                }
                setOnClickListener {
                    prefsManager.accentColor = hex
                    updateAccentColorDisplay()
                    dialog.dismiss()
                }
                contentDescription = name
            }
            container.addView(circle)
        }
        dialog.show()
    }

    private fun updateAccentColorDisplay() {
        val name = accentColors.find { it.second == prefsManager.accentColor }?.first ?: "Custom"
        findViewById<TextView>(R.id.tv_accent_color_value).text = name
        val preview = findViewById<View>(R.id.accent_color_preview)
        (preview.background as? GradientDrawable)?.setColor(Color.parseColor(prefsManager.accentColor))
            ?: run {
                preview.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(prefsManager.accentColor))
                }
            }
    }

    // ── Font Size ──

    private fun showFontSizePicker() {
        val options = arrayOf("Small", "Medium", "Large")
        val values = arrayOf("small", "medium", "large")
        val current = values.indexOf(prefsManager.fontSize).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Font Size")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefsManager.fontSize = values[which]
                updateFontSizeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateFontSizeDisplay() {
        val label = when (prefsManager.fontSize) {
            "small" -> "Small"
            "large" -> "Large"
            else -> "Medium"
        }
        findViewById<TextView>(R.id.tv_font_size_value).text = label
    }

    // ── Key Spacing ──

    private fun showKeySpacingPicker() {
        val options = arrayOf("Small (2dp)", "Medium (3dp)", "Large (5dp)")
        val values = arrayOf("small", "medium", "large")
        val current = values.indexOf(prefsManager.keySpacing).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Key Spacing")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefsManager.keySpacing = values[which]
                updateKeySpacingDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateKeySpacingDisplay() {
        val label = when (prefsManager.keySpacing) {
            "small" -> "Small"
            "large" -> "Large"
            else -> "Medium"
        }
        findViewById<TextView>(R.id.tv_key_spacing_value).text = label
    }

    // ── Bottom Padding ──

    private fun showBottomPaddingPicker() {
        val options = arrayOf("None (0dp)", "Small (8dp)", "Medium (16dp)", "Large (24dp)")
        val values = arrayOf(0, 8, 16, 24)
        val current = values.indexOf(prefsManager.bottomPadding).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Bottom Padding")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefsManager.bottomPadding = values[which]
                updateBottomPaddingDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBottomPaddingDisplay() {
        findViewById<TextView>(R.id.tv_bottom_padding_value).text = "${prefsManager.bottomPadding}dp"
    }

    // ── Popup Style ──

    private fun showPopupStylePicker() {
        val options = arrayOf("Balloon", "Minimal")
        val values = arrayOf("balloon", "minimal")
        val current = values.indexOf(prefsManager.popupStyle).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Popup Style")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefsManager.popupStyle = values[which]
                updatePopupStyleDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePopupStyleDisplay() {
        val label = when (prefsManager.popupStyle) {
            "minimal" -> "Minimal"
            else -> "Balloon"
        }
        findViewById<TextView>(R.id.tv_popup_style_value).text = label
    }

    // ── Preset ──

    private fun showPresetPicker() {
        val presetNames = KeyboardLayout.presetNames().toTypedArray()
        val current = presetNames.indexOf(prefsManager.selectedPreset).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle("Layout Preset")
            .setSingleChoiceItems(presetNames, current) { dialog, which ->
                val name = presetNames[which]
                prefsManager.selectedPreset = name
                layoutRepository.setPreset(name)
                updatePresetDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePresetDisplay() {
        findViewById<TextView>(R.id.tv_preset_value).text = prefsManager.selectedPreset
    }

    private fun requestMicPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }
    }

    override fun onResume() {
        super.onResume()
        updateKeyboardStatus()
        updatePresetDisplay()
        updateThemeDisplay()
        updateAccentColorDisplay()
        updateFontSizeDisplay()
        updateKeySpacingDisplay()
        updateBottomPaddingDisplay()
        updatePopupStyleDisplay()
    }

    private fun updateKeyboardStatus() {
        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }

        if (enabled) {
            tvStatus.text = getString(R.string.keyboard_enabled)
            tvStatus.setTextColor(getColor(R.color.key_green))
        } else {
            tvStatus.text = getString(R.string.keyboard_not_enabled)
            tvStatus.setTextColor(0xFFFF453A.toInt())
        }
    }
}
