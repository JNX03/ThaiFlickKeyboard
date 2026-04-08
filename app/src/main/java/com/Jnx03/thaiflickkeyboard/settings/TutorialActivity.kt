package com.Jnx03.thaiflickkeyboard.settings

import android.graphics.drawable.GradientDrawable
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

    private val dots = mutableListOf<android.view.View>()

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
        val dotSize = 8f.dpToPx(this).toInt()
        val dotMargin = 4f.dpToPx(this).toInt()

        for (i in 0 until TutorialPagerAdapter.STEP_COUNT) {
            val dot = android.view.View(this).apply {
                val shape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setSize(dotSize, dotSize)
                }
                background = shape
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    marginStart = dotMargin
                    marginEnd = dotMargin
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
            val shape = dots[i].background as GradientDrawable
            shape.setColor(if (i == selected) activeColor else inactiveColor)
        }
    }

    private fun finishTutorial() {
        prefsManager.tutorialSeen = true
        finish()
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
