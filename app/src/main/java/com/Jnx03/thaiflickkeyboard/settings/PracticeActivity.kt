package com.Jnx03.thaiflickkeyboard.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.PracticeWordList
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.util.ThemeManager

class PracticeActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    private lateinit var tvTargetWord: TextView
    private lateinit var etPractice: EditText
    private lateinit var tvResult: TextView
    private lateinit var tvProgressCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabEasy: TextView
    private lateinit var tabMedium: TextView
    private lateinit var tabHard: TextView
    private lateinit var tabAll: TextView

    private var currentLevel = PracticeWordList.LEVEL_EASY
    private var words: List<String> = emptyList()
    private var currentWordIndex = 0
    private var completedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        prefsManager = PreferencesManager(this)
        applyThemeMode()
        super.onCreate(savedInstanceState)
        ThemeManager.init(this, prefsManager.themeMode)
        setContentView(R.layout.activity_practice)

        tvTargetWord = findViewById(R.id.tv_target_word)
        etPractice = findViewById(R.id.et_practice)
        tvResult = findViewById(R.id.tv_result)
        tvProgressCount = findViewById(R.id.tv_progress_count)
        progressBar = findViewById(R.id.progress_bar)
        tabEasy = findViewById(R.id.tab_easy)
        tabMedium = findViewById(R.id.tab_medium)
        tabHard = findViewById(R.id.tab_hard)
        tabAll = findViewById(R.id.tab_all)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btn_skip).setOnClickListener { skipWord() }

        tabEasy.setOnClickListener { switchLevel(PracticeWordList.LEVEL_EASY) }
        tabMedium.setOnClickListener { switchLevel(PracticeWordList.LEVEL_MEDIUM) }
        tabHard.setOnClickListener { switchLevel(PracticeWordList.LEVEL_HARD) }
        tabAll.setOnClickListener { switchLevel(PracticeWordList.LEVEL_ALL) }

        etPractice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkInput(s?.toString()?.trim() ?: "")
            }
        })

        switchLevel(PracticeWordList.LEVEL_EASY)
    }

    private fun switchLevel(level: Int) {
        currentLevel = level
        words = PracticeWordList.getWordsForLevel(level)
        currentWordIndex = 0
        completedCount = 0
        updateTabStyles()
        showCurrentWord()
    }

    private fun updateTabStyles() {
        val activeColor = getColor(R.color.key_indigo)
        val inactiveColor = ThemeManager.currentColors.hintColor

        tabEasy.setTextColor(if (currentLevel == PracticeWordList.LEVEL_EASY) activeColor else inactiveColor)
        tabMedium.setTextColor(if (currentLevel == PracticeWordList.LEVEL_MEDIUM) activeColor else inactiveColor)
        tabHard.setTextColor(if (currentLevel == PracticeWordList.LEVEL_HARD) activeColor else inactiveColor)
        tabAll.setTextColor(if (currentLevel == PracticeWordList.LEVEL_ALL) activeColor else inactiveColor)

        tabEasy.textSize = if (currentLevel == PracticeWordList.LEVEL_EASY) 15f else 14f
        tabMedium.textSize = if (currentLevel == PracticeWordList.LEVEL_MEDIUM) 15f else 14f
        tabHard.textSize = if (currentLevel == PracticeWordList.LEVEL_HARD) 15f else 14f
        tabAll.textSize = if (currentLevel == PracticeWordList.LEVEL_ALL) 15f else 14f
    }

    private fun showCurrentWord() {
        if (currentWordIndex >= words.size) {
            tvTargetWord.text = ""
            tvResult.visibility = TextView.VISIBLE
            tvResult.text = getString(R.string.practice_complete)
            tvResult.setTextColor(getColor(R.color.key_indigo))
            etPractice.isEnabled = false
            clearHighlight()
            updateProgress()
            return
        }

        val word = words[currentWordIndex]
        tvTargetWord.text = word
        etPractice.setText("")
        etPractice.isEnabled = true
        tvResult.visibility = TextView.GONE
        highlightNextChar("", word)
        updateProgress()
    }

    private fun checkInput(input: String) {
        if (currentWordIndex >= words.size) return
        val target = words[currentWordIndex]

        highlightNextChar(input, target)

        if (input == target) {
            clearHighlight()
            completedCount++
            tvResult.visibility = TextView.VISIBLE
            tvResult.text = getString(R.string.tutorial_correct)
            tvResult.setTextColor(getColor(R.color.key_green))

            tvTargetWord.postDelayed({
                if (isFinishing) return@postDelayed
                currentWordIndex++
                showCurrentWord()
            }, 600)
        } else if (target.startsWith(input) && input.isNotEmpty()) {
            tvResult.visibility = TextView.VISIBLE
            tvResult.text = getString(R.string.tutorial_keep_going)
            tvResult.setTextColor(getColor(R.color.key_amber))
        } else if (input.isNotEmpty()) {
            tvResult.visibility = TextView.VISIBLE
            tvResult.text = getString(R.string.practice_try_again)
            tvResult.setTextColor(getColor(R.color.key_red))
        } else {
            tvResult.visibility = TextView.GONE
        }
    }

    private fun skipWord() {
        if (currentWordIndex >= words.size) return
        currentWordIndex++
        showCurrentWord()
    }

    private fun updateProgress() {
        val total = words.size
        tvProgressCount.text = "${completedCount}/${total}"
        progressBar.max = total
        progressBar.progress = completedCount
    }

    private fun highlightNextChar(input: String, target: String) {
        if (input.length < target.length) {
            setHighlightChar(target[input.length].toString())
        } else {
            clearHighlight()
        }
    }

    private fun setHighlightChar(char: String) {
        getSharedPreferences("tutorial_prefs", MODE_PRIVATE)
            .edit().putString("highlight_char", char).apply()
    }

    private fun clearHighlight() {
        getSharedPreferences("tutorial_prefs", MODE_PRIVATE)
            .edit().putString("highlight_char", "").apply()
    }

    override fun onPause() {
        super.onPause()
        clearHighlight()
    }

    override fun onDestroy() {
        clearHighlight()
        super.onDestroy()
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
