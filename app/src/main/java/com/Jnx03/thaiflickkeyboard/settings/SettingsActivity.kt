package com.Jnx03.thaiflickkeyboard.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)

        // Enable keyboard button
        findViewById<MaterialButton>(R.id.btn_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // Select keyboard button
        findViewById<MaterialButton>(R.id.btn_select).setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

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
