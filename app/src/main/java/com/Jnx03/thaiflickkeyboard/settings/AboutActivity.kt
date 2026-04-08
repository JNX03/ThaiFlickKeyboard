package com.Jnx03.thaiflickkeyboard.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.Jnx03.thaiflickkeyboard.BuildConfig
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeMode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.tv_version).text = "Version ${BuildConfig.VERSION_NAME}"

        findViewById<LinearLayout>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.row_github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnx03/ThaiFlickKeyboard")))
        }

        findViewById<LinearLayout>(R.id.row_developer).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnx03")))
        }
    }

    private fun applyThemeMode() {
        val mode = PreferencesManager(this).themeMode
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
