package com.Jnx03.thaiflickkeyboard.settings

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.settings.tutorial.TutorialPagerAdapter
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx

class TutorialActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var btnNext: TextView
    private lateinit var btnSkip: TextView
    private lateinit var prefsManager: PreferencesManager

    private val dots = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        prefsManager = PreferencesManager(this)
        applyThemeMode()
        super.onCreate(savedInstanceState)
        ThemeManager.init(this, prefsManager.themeMode)
        setContentView(R.layout.activity_tutorial)

        viewPager = findViewById(R.id.view_pager)
        dotsContainer = findViewById(R.id.dots_container)
        btnNext = findViewById(R.id.btn_next)
        btnSkip = findViewById(R.id.btn_skip)

        viewPager.adapter = TutorialPagerAdapter(this)

        setupDots()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                btnNext.text = if (position == TutorialPagerAdapter.STEP_COUNT - 1)
                    getString(R.string.tutorial_done) else getString(R.string.tutorial_next)
            }
        })

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < TutorialPagerAdapter.STEP_COUNT - 1) {
                viewPager.currentItem = current + 1
            } else {
                finishTutorial()
            }
        }

        btnSkip.setOnClickListener {
            finishTutorial()
        }
    }

    private fun setupDots() {
        dotsContainer.removeAllViews()
        dots.clear()
        val margin = 2f.dpToPx(this).toInt()

        for (i in 0 until TutorialPagerAdapter.STEP_COUNT) {
            val dot = TextView(this).apply {
                text = "\u2022" // bullet
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = margin
                    marginEnd = margin
                }
            }
            dots.add(dot)
            dotsContainer.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(selected: Int) {
        val activeColor = getColor(R.color.key_indigo)
        val inactiveColor = ThemeManager.currentColors.hintColor

        for (i in dots.indices) {
            if (i == selected) {
                dots[i].text = "☝"
                dots[i].textSize = 18f
                dots[i].setTextColor(activeColor)
            } else {
                dots[i].text = "\u2022"
                dots[i].textSize = 16f
                dots[i].setTextColor(inactiveColor)
            }
        }
    }

    private fun finishTutorial() {
        prefsManager.tutorialSeen = true
        // Clear any tutorial highlight
        getSharedPreferences("tutorial_prefs", MODE_PRIVATE)
            .edit().putString("highlight_char", "").apply()

        // Offer feedback before closing
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_ThaiFlickKeyboard_Dialog)
            .setTitle(getString(R.string.tutorial_feedback_title))
            .setMessage(getString(R.string.tutorial_feedback_message))
            .setPositiveButton(getString(R.string.tutorial_feedback_github)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/JNX03/ThaiFlickKeyboard/issues/new")))
                finish()
            }
            .setNegativeButton(getString(R.string.tutorial_feedback_skip)) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
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
