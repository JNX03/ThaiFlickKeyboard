package com.Jnx03.thaiflickkeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import java.lang.ref.WeakReference

class InputActionHandler(service: InputMethodService) {

    private val serviceRef = WeakReference(service)

    private val inputConnection get() = serviceRef.get()?.currentInputConnection

    fun commitChar(text: String) {
        inputConnection?.commitText(text, 1)
    }

    fun deleteBackward() {
        inputConnection?.deleteSurroundingText(1, 0)
    }

    fun sendEnter() {
        inputConnection?.apply {
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    fun sendSpace() {
        commitChar(" ")
    }

    fun moveCursorLeft() {
        inputConnection?.apply {
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
        }
    }

    fun moveCursorRight() {
        inputConnection?.apply {
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
        }
    }
}
