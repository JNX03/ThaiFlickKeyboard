package com.Jnx03.thaiflickkeyboard.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var layoutRepository: LayoutRepository

    private val presetMap = mapOf(
        R.id.rb_optimized_center to "Optimized (Center)",
        R.id.rb_optimized_right to "Optimized (Right Hand)",
        R.id.rb_optimized_left to "Optimized (Left Hand)",
        R.id.rb_phonetic to "Phonetic (Original)",
        R.id.rb_speed to "Speed (Experimental)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)
        layoutRepository = LayoutRepository(this)

        // Enable keyboard button
        findViewById<MaterialButton>(R.id.btn_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // Select keyboard button
        findViewById<MaterialButton>(R.id.btn_select).setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // Layout preset selection
        setupPresetSelector()

        // Customize layout button
        findViewById<MaterialButton>(R.id.btn_customize).setOnClickListener {
            startActivity(Intent(this, LayoutCustomizationActivity::class.java))
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
    }

    private fun setupPresetSelector() {
        val radioGroup = findViewById<RadioGroup>(R.id.rg_presets)
        val tvDesc = findViewById<TextView>(R.id.tv_preset_desc)

        // Set current selection
        val currentPreset = prefsManager.selectedPreset
        val currentId = presetMap.entries.find { it.value == currentPreset }?.key
            ?: R.id.rb_optimized_center
        radioGroup.check(currentId)

        // Show description for current preset
        updatePresetDescription(tvDesc, currentPreset)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val presetName = presetMap[checkedId] ?: return@setOnCheckedChangeListener
            prefsManager.selectedPreset = presetName
            layoutRepository.setPreset(presetName)
            updatePresetDescription(tvDesc, presetName)
        }
    }

    private fun updatePresetDescription(tvDesc: TextView, presetName: String) {
        val layout = KeyboardLayout.fromPresetName(presetName)
        tvDesc.text = layout.description
    }

    override fun onResume() {
        super.onResume()
        updateKeyboardStatus()
    }

    private fun updateKeyboardStatus() {
        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any {
            it.packageName == packageName
        }

        if (enabled) {
            tvStatus.text = getString(R.string.keyboard_enabled)
            tvStatus.setTextColor(getColor(R.color.key_green))
        } else {
            tvStatus.text = getString(R.string.keyboard_not_enabled)
            tvStatus.setTextColor(getColor(R.color.key_red))
        }
    }
}
