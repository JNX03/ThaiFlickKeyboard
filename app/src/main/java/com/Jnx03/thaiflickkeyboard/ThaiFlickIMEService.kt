package com.Jnx03.thaiflickkeyboard

import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.data.ThaiWordList
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.model.KeyboardMode
import com.Jnx03.thaiflickkeyboard.util.HapticHelper
import com.Jnx03.thaiflickkeyboard.view.FlickKeyboardView
import com.Jnx03.thaiflickkeyboard.view.SuggestionBarView
import com.Jnx03.thaiflickkeyboard.view.UtilityBarView

class ThaiFlickIMEService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var layoutRepository: LayoutRepository
    private lateinit var prefsManager: PreferencesManager
    private lateinit var actionHandler: InputActionHandler

    private var keyboardView: FlickKeyboardView? = null
    private var utilityBar: UtilityBarView? = null
    private var suggestionBar: SuggestionBarView? = null
    private var currentMode = KeyboardMode.THAI

    override fun onCreate() {
        super.onCreate()
        layoutRepository = LayoutRepository(this)
        prefsManager = PreferencesManager(this)
        actionHandler = InputActionHandler(this)
        layoutRepository.registerChangeListener(this)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_container, null)

        suggestionBar = view.findViewById<SuggestionBarView>(R.id.suggestion_bar).apply {
            onSuggestionSelected = { word ->
                commitSuggestion(word)
            }
        }

        keyboardView = view.findViewById<FlickKeyboardView>(R.id.keyboard_view).apply {
            layout = loadLayoutForCurrentMode()
            hapticEnabled = prefsManager.hapticEnabled

            onCharacterSelected = { char ->
                if (prefsManager.hapticEnabled) {
                    HapticHelper.performKeyPress(this)
                }
                actionHandler.commitChar(char)
                updateSuggestions()
            }

            onSpecialKey = { key ->
                when (key) {
                    "backspace" -> {
                        if (prefsManager.hapticEnabled) {
                            HapticHelper.performKeyPress(this)
                        }
                        actionHandler.deleteBackward()
                        updateSuggestions()
                    }
                    "mode" -> switchMode()
                }
            }
        }

        utilityBar = view.findViewById<UtilityBarView>(R.id.utility_bar).apply {
            onSpace = {
                if (prefsManager.hapticEnabled) {
                    HapticHelper.performKeyPress(this)
                }
                actionHandler.sendSpace()
                updateSuggestions()
            }
            onEnter = {
                if (prefsManager.hapticEnabled) {
                    HapticHelper.performKeyPress(this)
                }
                actionHandler.sendEnter()
                suggestionBar?.setSuggestions(emptyList())
            }
            onLanguageSwitch = {
                switchMode()
            }
            onCursorLeft = {
                actionHandler.moveCursorLeft()
            }
            onCursorRight = {
                actionHandler.moveCursorRight()
            }
        }

        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        updateSuggestions()
    }

    private fun updateSuggestions() {
        if (currentMode != KeyboardMode.THAI) {
            suggestionBar?.setSuggestions(emptyList())
            return
        }
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""

        // Extract the last word (text after last space)
        val lastWord = textBefore.split(" ", "\n").lastOrNull() ?: ""

        if (lastWord.isEmpty()) {
            suggestionBar?.setSuggestions(emptyList())
            return
        }

        val suggestions = ThaiWordList.getSuggestions(lastWord, 3)
        suggestionBar?.setSuggestions(suggestions)
    }

    private fun commitSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
        val lastWord = textBefore.split(" ", "\n").lastOrNull() ?: ""

        // Delete the typed prefix and commit the full word
        if (lastWord.isNotEmpty()) {
            ic.deleteSurroundingText(lastWord.length, 0)
        }
        ic.commitText("$word ", 1)
        suggestionBar?.setSuggestions(emptyList())
    }

    private fun loadLayoutForCurrentMode(): KeyboardLayout {
        return when (currentMode) {
            KeyboardMode.THAI -> layoutRepository.loadLayout()
            KeyboardMode.ENGLISH -> KeyboardLayout.english()
            KeyboardMode.NUMBERS -> KeyboardLayout.numbers()
        }
    }

    private fun switchMode() {
        currentMode = when (currentMode) {
            KeyboardMode.THAI -> KeyboardMode.ENGLISH
            KeyboardMode.ENGLISH -> KeyboardMode.NUMBERS
            KeyboardMode.NUMBERS -> KeyboardMode.THAI
        }
        keyboardView?.layout = loadLayoutForCurrentMode()
        updateSuggestions()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (currentMode == KeyboardMode.THAI) {
            keyboardView?.layout = layoutRepository.loadLayout()
        }
        keyboardView?.hapticEnabled = prefsManager.hapticEnabled
    }

    override fun onDestroy() {
        layoutRepository.unregisterChangeListener(this)
        super.onDestroy()
    }
}
