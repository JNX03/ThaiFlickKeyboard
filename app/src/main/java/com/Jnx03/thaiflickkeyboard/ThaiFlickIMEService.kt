package com.Jnx03.thaiflickkeyboard

import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.Jnx03.thaiflickkeyboard.data.ClipboardHistoryManager
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.data.PreferencesManager
import com.Jnx03.thaiflickkeyboard.data.ThaiWordList
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.model.KeyboardMode
import com.Jnx03.thaiflickkeyboard.util.HapticHelper
import com.Jnx03.thaiflickkeyboard.view.ClipboardPanelView
import com.Jnx03.thaiflickkeyboard.view.FlickKeyboardView
import com.Jnx03.thaiflickkeyboard.view.QwertyKeyboardView
import com.Jnx03.thaiflickkeyboard.view.SuggestionBarView
import com.Jnx03.thaiflickkeyboard.view.ToolbarView

class ThaiFlickIMEService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var layoutRepository: LayoutRepository
    private lateinit var prefsManager: PreferencesManager
    private lateinit var actionHandler: InputActionHandler
    private lateinit var clipboardHistory: ClipboardHistoryManager

    private var keyboardView: FlickKeyboardView? = null
    private var qwertyView: QwertyKeyboardView? = null
    private var suggestionBar: SuggestionBarView? = null
    private var clipboardPanel: ClipboardPanelView? = null
    private var currentMode = KeyboardMode.THAI
    private var clipboardShowing = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isSpeechListening = false

    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onCreate() {
        super.onCreate()
        layoutRepository = LayoutRepository(this)
        prefsManager = PreferencesManager(this)
        actionHandler = InputActionHandler(this)
        clipboardHistory = ClipboardHistoryManager(this)
        layoutRepository.registerChangeListener(this)

        // Listen for clipboard changes
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) clipboardHistory.addClip(text)
            }
        }
        cm.addPrimaryClipChangedListener(clipListener)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_container, null)

        // Push keyboard above the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBarHeight)
            insets
        }

        // Toolbar
        view.findViewById<ToolbarView>(R.id.toolbar_view).apply {
            onSettings = {
                val intent = Intent(this@ThaiFlickIMEService, com.Jnx03.thaiflickkeyboard.settings.SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            onMic = { startSpeechRecognition() }
            onEmoji = { showSystemEmoji() }
            onClipboard = { toggleClipboardPanel() }
        }

        suggestionBar = view.findViewById<SuggestionBarView>(R.id.suggestion_bar).apply {
            onSuggestionSelected = { word -> commitSuggestion(word) }
        }

        keyboardView = view.findViewById<FlickKeyboardView>(R.id.keyboard_view).apply {
            layout = layoutRepository.loadLayout()
            hapticEnabled = prefsManager.hapticEnabled
            onCharacterSelected = { char ->
                if (prefsManager.hapticEnabled) HapticHelper.performKeyPress(this)
                actionHandler.commitChar(char)
                updateSuggestions()
            }
            onSpecialKey = { key ->
                when (key) {
                    "backspace" -> {
                        if (prefsManager.hapticEnabled) HapticHelper.performKeyPress(this)
                        actionHandler.deleteBackward()
                        updateSuggestions()
                    }
                    "mode" -> switchMode()
                }
            }
            onSpace = {
                if (prefsManager.hapticEnabled) HapticHelper.performKeyPress(this)
                actionHandler.sendSpace()
                updateSuggestions()
            }
            onEnter = {
                if (prefsManager.hapticEnabled) HapticHelper.performKeyPress(this)
                actionHandler.sendEnter()
                suggestionBar?.setSuggestions(emptyList())
            }
            onCursorLeft = { actionHandler.moveCursorLeft() }
            onCursorRight = { actionHandler.moveCursorRight() }
            onMicPressed = { startSpeechRecognition() }
            onEmojiPressed = { handleEmojiOrShift() }
            onTopRowUpBalloon = { displayChar, centerX, width, isActive ->
                suggestionBar?.showFlickBalloon(displayChar, centerX, width, isActive)
            }
        }

        qwertyView = view.findViewById<QwertyKeyboardView>(R.id.qwerty_view).apply {
            hapticEnabled = prefsManager.hapticEnabled
            onCharacterSelected = { char -> actionHandler.commitChar(char) }
            onBackspace = { actionHandler.deleteBackward() }
            onSpace = { actionHandler.sendSpace() }
            onEnter = { actionHandler.sendEnter() }
            onSwitchMode = { switchMode() }
        }

        clipboardPanel = view.findViewById<ClipboardPanelView>(R.id.clipboard_panel).apply {
            onPaste = { text ->
                currentInputConnection?.commitText(text, 1)
                hideClipboardPanel()
            }
            onClose = { hideClipboardPanel() }
            onDelete = { index ->
                clipboardHistory.removeClip(index)
                setHistory(clipboardHistory.getHistory())
            }
            onClearAll = {
                clipboardHistory.clearAll()
                setHistory(emptyList())
            }
        }

        updateModeLabel()
        updateShiftIcon()
        showKeyboardForMode()
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (clipboardShowing) hideClipboardPanel()
        updateSuggestions()
    }

    // ── Clipboard Panel ──

    private fun toggleClipboardPanel() {
        if (clipboardShowing) hideClipboardPanel()
        else showClipboardPanel()
    }

    private fun showClipboardPanel() {
        clipboardPanel?.setHistory(clipboardHistory.getHistory())
        keyboardView?.visibility = View.GONE
        qwertyView?.visibility = View.GONE
        clipboardPanel?.visibility = View.VISIBLE
        clipboardShowing = true
    }

    private fun hideClipboardPanel() {
        clipboardPanel?.visibility = View.GONE
        clipboardShowing = false
        showKeyboardForMode()
    }

    // ── Mode Switching ──

    private fun showKeyboardForMode() {
        if (clipboardShowing) return
        when (currentMode) {
            KeyboardMode.ENGLISH -> {
                keyboardView?.visibility = View.GONE
                qwertyView?.visibility = View.VISIBLE
            }
            else -> {
                keyboardView?.visibility = View.VISIBLE
                qwertyView?.visibility = View.GONE
            }
        }
    }

    private fun switchMode() {
        currentMode = when (currentMode) {
            KeyboardMode.THAI -> KeyboardMode.NUMBERS
            KeyboardMode.THAI_SHIFT -> KeyboardMode.NUMBERS
            KeyboardMode.NUMBERS -> KeyboardMode.ENGLISH
            KeyboardMode.ENGLISH -> KeyboardMode.THAI
            KeyboardMode.EMOJI -> KeyboardMode.THAI
        }
        when (currentMode) {
            KeyboardMode.THAI -> keyboardView?.layout = layoutRepository.loadLayout()
            KeyboardMode.NUMBERS -> keyboardView?.layout = KeyboardLayout.numbers()
            else -> {}
        }
        updateModeLabel()
        updateShiftIcon()
        showKeyboardForMode()
        updateSuggestions()
    }

    private fun handleEmojiOrShift() {
        if (currentMode == KeyboardMode.THAI) {
            currentMode = KeyboardMode.THAI_SHIFT
            keyboardView?.layout = KeyboardLayout.thaiShift()
            updateShiftIcon()
        } else if (currentMode == KeyboardMode.THAI_SHIFT) {
            currentMode = KeyboardMode.THAI
            keyboardView?.layout = layoutRepository.loadLayout()
            updateShiftIcon()
        } else {
            showSystemEmoji()
        }
    }

    private fun updateShiftIcon() {
        keyboardView?.showShiftIcon = (currentMode == KeyboardMode.THAI || currentMode == KeyboardMode.THAI_SHIFT)
    }

    private fun updateModeLabel() {
        keyboardView?.modeLabel = when (currentMode) {
            KeyboardMode.THAI -> "123"
            KeyboardMode.THAI_SHIFT -> "123"
            KeyboardMode.NUMBERS -> "ABC"
            KeyboardMode.ENGLISH -> "ก"
            KeyboardMode.EMOJI -> "ก"
        }
    }

    // ── Suggestions ──

    private fun updateSuggestions() {
        if (currentMode != KeyboardMode.THAI && currentMode != KeyboardMode.THAI_SHIFT) {
            suggestionBar?.setSuggestions(emptyList())
            return
        }
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
        val lastWord = textBefore.split(" ", "\n").lastOrNull() ?: ""
        if (lastWord.isEmpty()) { suggestionBar?.setSuggestions(emptyList()); return }
        suggestionBar?.setSuggestions(ThaiWordList.getSuggestions(lastWord, 3))
    }

    private fun commitSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
        val lastWord = textBefore.split(" ", "\n").lastOrNull() ?: ""
        if (lastWord.isNotEmpty()) ic.deleteSurroundingText(lastWord.length, 0)
        ic.commitText("$word ", 1)
        suggestionBar?.setSuggestions(emptyList())
    }

    // ── Emoji ──

    private fun showSystemEmoji() {
        val imeManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imeManager.showInputMethodPicker()
    }

    // ── Speech-to-Text ──

    private fun startSpeechRecognition() {
        if (isSpeechListening) {
            speechRecognizer?.stopListening()
            isSpeechListening = false
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isSpeechListening = false }
                override fun onError(error: Int) { isSpeechListening = false }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        currentInputConnection?.commitText(matches[0], 1)
                        updateSuggestions()
                    }
                    isSpeechListening = false
                }
            })
        }
        val lang = if (currentMode == KeyboardMode.THAI) "th-TH" else "en-US"
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
            isSpeechListening = true
        } catch (e: SecurityException) {
            Toast.makeText(this, "Microphone permission needed — enable in Settings", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (currentMode == KeyboardMode.THAI) keyboardView?.layout = layoutRepository.loadLayout()
        keyboardView?.hapticEnabled = prefsManager.hapticEnabled
        qwertyView?.hapticEnabled = prefsManager.hapticEnabled
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipListener?.let { cm.removePrimaryClipChangedListener(it) }
        layoutRepository.unregisterChangeListener(this)
        super.onDestroy()
    }
}
