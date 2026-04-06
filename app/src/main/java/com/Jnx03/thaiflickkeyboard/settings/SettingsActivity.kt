package com.Jnx03.thaiflickkeyboard.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var layoutRepository: LayoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)
        layoutRepository = LayoutRepository(this)

        // Enable keyboard
        findViewById<LinearLayout>(R.id.row_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // Select keyboard
        findViewById<LinearLayout>(R.id.row_select).setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // Layout preset - show dialog picker
        findViewById<LinearLayout>(R.id.row_preset).setOnClickListener {
            showPresetPicker()
        }
        updatePresetDisplay()

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

        requestMicPermissionIfNeeded()
    }

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
