package com.Jnx03.thaiflickkeyboard

import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.view.View
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.model.KeyboardMode
import com.Jnx03.thaiflickkeyboard.util.HapticHelper
import com.Jnx03.thaiflickkeyboard.view.FlickKeyboardView
import com.Jnx03.thaiflickkeyboard.view.UtilityBarView

class ThaiFlickIMEService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var layoutRepository: LayoutRepository
    private lateinit var prefsManager: PreferencesManager
    private lateinit var actionHandler: InputActionHandler

    private var keyboardView: FlickKeyboardView? = null
    private var utilityBar: UtilityBarView? = null
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

        keyboardView = view.findViewById<FlickKeyboardView>(R.id.keyboard_view).apply {
            layout = loadLayoutForCurrentMode()
            hapticEnabled = prefsManager.hapticEnabled

            onCharacterSelected = { char ->
                if (prefsManager.hapticEnabled) {
                    HapticHelper.performKeyPress(this)
                }
                actionHandler.commitChar(char)
            }

            onSpecialKey = { key ->
                when (key) {
                    "backspace" -> {
                        if (prefsManager.hapticEnabled) {
                            HapticHelper.performKeyPress(this)
                        }
                        actionHandler.deleteBackward()
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
            }
            onEnter = {
                if (prefsManager.hapticEnabled) {
                    HapticHelper.performKeyPress(this)
                }
                actionHandler.sendEnter()
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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Reload layout if it was changed from settings
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
