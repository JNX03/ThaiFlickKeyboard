package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.gesture.FlickGestureDetector
import com.Jnx03.thaiflickkeyboard.model.FlickDirection
import com.Jnx03.thaiflickkeyboard.model.FlickKey
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

/**
 * Full keyboard view with integrated utility keys.
 * Layout: 5 columns x 4 rows (Japanese flick keyboard style)
 *
 *   [🎤 Mic]  [k1] [k2] [k3]  [⌫]
 *   [◀]       [k4] [k5] [k6]  [▶]
 *   [123]     [k7] [k8] [k9]  [Space/Tones]
 *   [😀]      [v1] [v2] [v3]  [↵ Enter]
 *
 * Character keys (cols 1-3): flick-enabled, from KeyboardLayout
 * Utility keys (cols 0,4): tap-only, except Space (flick=tones) & Emoji (flick=special)
 */
class FlickKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Character layout (12 keys for the 3x4 center grid)
    var layout: KeyboardLayout = KeyboardLayout.default()
        set(value) {
            field = value
            charKeys = value.keys.filter {
                it.id != "backspace" && it.id != "mode" && it.id != "tone" && it.id != "special"
            }.take(12)
            invalidate()
        }

    var onCharacterSelected: ((String) -> Unit)? = null
    var onSpecialKey: ((String) -> Unit)? = null
    var onSpace: (() -> Unit)? = null
    var onEnter: (() -> Unit)? = null
    var onCursorLeft: (() -> Unit)? = null
    var onCursorRight: (() -> Unit)? = null
    var onMicPressed: (() -> Unit)? = null
    var onEmojiPressed: (() -> Unit)? = null
    var hapticEnabled: Boolean = true
    var modeLabel: String = "123"
        set(value) { field = value; invalidate() }

    private var charKeys: List<FlickKey> = emptyList()

    // Space key: tap=space, flick=tone marks (like Japanese dakuten key)
    private val toneSpaceKey = FlickKey("space_tone", "Space", "่", "์", "้", "๊", "#a855f7", "")
    // Emoji key: tap=emoji, flick=special Thai chars
    private val emojiSpecialKey = FlickKey("emoji_special", "😀", "ๆ", "ฯ", "็", "๋", "#64748b", "")

    private val gestureDetector = FlickGestureDetector(20f.dpToPx(context))
    private val crossPopup = FlickCrossPopup(context)

    // Touch state
    private var activeCol = -1
    private var activeRow = -1
    private var currentDirection = FlickDirection.TAP
    private var isTouching = false

    // Grid dimensions
    private val COLS = 5
    private val ROWS = 4
    private val utilColRatio = 0.14f  // utility columns width ratio
    private val charColRatio = (1f - 2 * utilColRatio) / 3f // character columns

    private var colWidths = FloatArray(COLS)
    private var colStarts = FloatArray(COLS)
    private var rowHeight = 0f

    // Colors
    private val kbBg = Color.parseColor("#2b2b2b")
    private val charKeyBg = Color.parseColor("#4a4a52")
    private val charKeyPressed = Color.parseColor("#5a5a65")
    private val utilKeyBg = Color.parseColor("#36363e")
    private val utilKeyPressed = Color.parseColor("#4a4a52")
    private val textColor = Color.parseColor("#e8e8e8")
    private val hintColor = Color.parseColor("#707078")
    private val accentColor = Color.parseColor("#5b9bf5")

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = hintColor
        textAlign = Paint.Align.CENTER
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
    }

    private val pad = 3f.dpToPx(context)
    private val cornerR = 8f.dpToPx(context)
    private val rect = RectF()

    private val backspaceIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_backspace)
    private val enterIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_enter)
    private val micIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_mic)
    private val emojiIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_emoji)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (resources.displayMetrics.heightPixels * 0.30f).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val utilW = w * utilColRatio
        val charW = w * charColRatio
        colWidths = floatArrayOf(utilW, charW, charW, charW, utilW)
        colStarts[0] = 0f
        for (i in 1 until COLS) colStarts[i] = colStarts[i - 1] + colWidths[i - 1]
        rowHeight = h.toFloat() / ROWS
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(kbBg)

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val x = colStarts[col] + pad
                val y = row * rowHeight + pad
                val w = colWidths[col] - pad * 2
                val h = rowHeight - pad * 2
                rect.set(x, y, x + w, y + h)

                val isActive = isTouching && activeCol == col && activeRow == row
                val isCharKey = col in 1..3

                if (isCharKey) {
                    drawCharKey(canvas, row, col, rect, isActive)
                } else {
                    drawUtilKey(canvas, row, col, rect, isActive)
                }
            }
        }
    }

    private fun drawCharKey(canvas: Canvas, row: Int, col: Int, rect: RectF, isActive: Boolean) {
        val keyIndex = row * 3 + (col - 1)
        if (keyIndex >= charKeys.size) return
        val key = charKeys[keyIndex]

        // Background
        keyBgPaint.color = if (isActive) charKeyPressed else charKeyBg
        canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)

        // Category color accent bar
        try {
            accentPaint.color = Color.parseColor(key.color)
            accentPaint.alpha = 160
            canvas.drawRoundRect(
                rect.left + cornerR, rect.top, rect.right - cornerR, rect.top + 3f.dpToPx(context),
                2f, 2f, accentPaint
            )
        } catch (_: Exception) {}

        val cx = rect.centerX()
        val cy = rect.centerY()

        // Main character (large)
        textPaint.textSize = 26f.spToPx(context)
        textPaint.color = textColor
        canvas.drawText(toDisplayChar(key.tap), cx, cy + 4f.dpToPx(context), textPaint)

        // Direction hints (subtle, at edges)
        if (!isActive) {
            hintPaint.textSize = 9f.spToPx(context)
            val ox = rect.width() * 0.30f
            val oy = rect.height() * 0.30f
            if (key.up.isNotEmpty())
                canvas.drawText(toDisplayChar(key.up), cx, rect.top + 14f.dpToPx(context), hintPaint)
            if (key.down.isNotEmpty())
                canvas.drawText(toDisplayChar(key.down), cx, rect.bottom - 5f.dpToPx(context), hintPaint)
            if (key.left.isNotEmpty())
                canvas.drawText(toDisplayChar(key.left), rect.left + 14f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
            if (key.right.isNotEmpty())
                canvas.drawText(toDisplayChar(key.right), rect.right - 14f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
        }

        // Hint label
        if (!isActive && key.hint.isNotEmpty()) {
            hintPaint.textSize = 7f.spToPx(context)
            canvas.drawText(key.hint, cx, rect.bottom - 2f.dpToPx(context), hintPaint)
        }
    }

    private fun drawUtilKey(canvas: Canvas, row: Int, col: Int, rect: RectF, isActive: Boolean) {
        keyBgPaint.color = if (isActive) utilKeyPressed else utilKeyBg
        canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)

        val cx = rect.centerX()
        val cy = rect.centerY()
        textPaint.color = textColor

        when {
            // Left column (col 0)
            col == 0 && row == 0 -> { // Mic (speech-to-text)
                drawIcon(canvas, micIcon, rect, 0.40f)
            }
            col == 0 && row == 1 -> { // Left arrow
                textPaint.textSize = 20f.spToPx(context)
                canvas.drawText("◀", cx, cy + 6f.dpToPx(context), textPaint)
            }
            col == 0 && row == 2 -> { // Mode switch (123/ABC/ก)
                textPaint.textSize = 14f.spToPx(context)
                textPaint.color = accentColor
                canvas.drawText(modeLabel, cx, cy + 5f.dpToPx(context), textPaint)
                textPaint.color = textColor
            }
            col == 0 && row == 3 -> { // Emoji (tap) + Special chars (flick)
                drawIcon(canvas, emojiIcon, rect, 0.35f)
                if (!isActive) {
                    hintPaint.textSize = 8f.spToPx(context)
                    if (emojiSpecialKey.up.isNotEmpty())
                        canvas.drawText(emojiSpecialKey.up, cx, rect.top + 11f.dpToPx(context), hintPaint)
                    if (emojiSpecialKey.down.isNotEmpty())
                        canvas.drawText(emojiSpecialKey.down, cx, rect.bottom - 4f.dpToPx(context), hintPaint)
                    if (emojiSpecialKey.left.isNotEmpty())
                        canvas.drawText(emojiSpecialKey.left, rect.left + 10f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                    if (emojiSpecialKey.right.isNotEmpty())
                        canvas.drawText(toDisplayChar(emojiSpecialKey.right), rect.right - 10f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                }
            }
            // Right column (col 4)
            col == 4 && row == 0 -> { // Backspace
                drawIcon(canvas, backspaceIcon, rect, 0.45f)
            }
            col == 4 && row == 1 -> { // Right arrow
                textPaint.textSize = 20f.spToPx(context)
                canvas.drawText("▶", cx, cy + 6f.dpToPx(context), textPaint)
            }
            col == 4 && row == 2 -> { // Space (tap) + Tone marks (flick)
                textPaint.textSize = 11f.spToPx(context)
                canvas.drawText("Space", cx, cy + 4f.dpToPx(context), textPaint)
                if (!isActive) {
                    hintPaint.textSize = 8f.spToPx(context)
                    if (toneSpaceKey.up.isNotEmpty())
                        canvas.drawText(toDisplayChar(toneSpaceKey.up), cx, rect.top + 11f.dpToPx(context), hintPaint)
                    if (toneSpaceKey.down.isNotEmpty())
                        canvas.drawText(toDisplayChar(toneSpaceKey.down), cx, rect.bottom - 4f.dpToPx(context), hintPaint)
                    if (toneSpaceKey.left.isNotEmpty())
                        canvas.drawText(toDisplayChar(toneSpaceKey.left), rect.left + 10f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                    if (toneSpaceKey.right.isNotEmpty())
                        canvas.drawText(toDisplayChar(toneSpaceKey.right), rect.right - 10f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                }
            }
            col == 4 && row == 3 -> { // Enter
                drawIcon(canvas, enterIcon, rect, 0.40f)
            }
        }
    }

    private fun drawIcon(canvas: Canvas, icon: Drawable?, rect: RectF, scale: Float) {
        icon?.let {
            val size = (rect.height() * scale).toInt()
            val cx = rect.centerX().toInt()
            val cy = rect.centerY().toInt()
            it.setBounds(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
            it.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val col = getCol(event.x)
                val row = getRow(event.y)
                if (col < 0 || row < 0) return true

                activeCol = col
                activeRow = row
                isTouching = true
                currentDirection = FlickDirection.TAP
                gestureDetector.onTouchDown(event.x, event.y)
                invalidate()

                // Handle backspace immediately
                if (col == 4 && row == 0) {
                    onSpecialKey?.invoke("backspace")
                    startBackspaceRepeat()
                }

                // Show cross popup for flick keys
                val flickKey = getFlickKeyAt(col, row)
                if (flickKey != null) {
                    showCrossPopup(flickKey, col, row)
                }

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouching) {
                    val flickKey = getFlickKeyAt(activeCol, activeRow)
                    if (flickKey != null) {
                        val newDir = gestureDetector.onTouchMove(event.x, event.y)
                        if (newDir != currentDirection) {
                            currentDirection = newDir
                            crossPopup.updateDirection(newDir)
                            invalidate()
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopBackspaceRepeat()
                if (isTouching) {
                    val direction = gestureDetector.onTouchUp()
                    handleKeyUp(activeCol, activeRow, direction)
                }
                crossPopup.dismiss()
                activeCol = -1
                activeRow = -1
                isTouching = false
                currentDirection = FlickDirection.TAP
                gestureDetector.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleKeyUp(col: Int, row: Int, direction: FlickDirection) {
        // Space key: tap=space, flick=tone marks
        if (col == 4 && row == 2) {
            if (direction == FlickDirection.TAP) {
                onSpace?.invoke()
            } else {
                val char = toneSpaceKey.charForDirection(direction)
                if (char.isNotEmpty() && char != "Space") onCharacterSelected?.invoke(char)
            }
            return
        }

        // Emoji key: tap=emoji, flick=special Thai chars
        if (col == 0 && row == 3) {
            if (direction == FlickDirection.TAP) {
                onEmojiPressed?.invoke()
            } else {
                val char = emojiSpecialKey.charForDirection(direction)
                if (char.isNotEmpty()) onCharacterSelected?.invoke(char)
            }
            return
        }

        // Character keys (cols 1-3)
        val flickKey = getFlickKeyAt(col, row)
        if (flickKey != null) {
            val char = flickKey.charForDirection(direction)
            if (char.isNotEmpty()) {
                onCharacterSelected?.invoke(char)
            }
            return
        }

        // Utility keys
        when {
            col == 0 && row == 0 -> onMicPressed?.invoke()
            col == 0 && row == 1 -> onCursorLeft?.invoke()
            col == 0 && row == 2 -> onSpecialKey?.invoke("mode")
            col == 4 && row == 0 -> { /* backspace already handled */ }
            col == 4 && row == 1 -> onCursorRight?.invoke()
            col == 4 && row == 3 -> onEnter?.invoke()
        }
    }

    private fun getFlickKeyAt(col: Int, row: Int): FlickKey? {
        // Character keys in cols 1-3
        if (col in 1..3) {
            val index = row * 3 + (col - 1)
            return charKeys.getOrNull(index)
        }
        // Space key (col 4, row 2): flick for tone marks
        if (col == 4 && row == 2) return toneSpaceKey
        // Emoji key (col 0, row 3): flick for special chars
        if (col == 0 && row == 3) return emojiSpecialKey
        return null
    }

    private fun showCrossPopup(key: FlickKey, col: Int, row: Int) {
        val location = IntArray(2)
        getLocationInWindow(location)
        val anchorX = location[0] + (colStarts[col] + colWidths[col] / 2).toInt()
        val anchorY = location[1] + (row * rowHeight).toInt()
        crossPopup.show(this, key, FlickDirection.TAP, anchorX, anchorY)
    }

    private fun getCol(x: Float): Int {
        for (i in 0 until COLS) {
            if (x >= colStarts[i] && x < colStarts[i] + colWidths[i]) return i
        }
        return -1
    }

    private fun getRow(y: Float): Int {
        val r = (y / rowHeight).toInt()
        return if (r in 0 until ROWS) r else -1
    }

    // Backspace repeat
    private val backspaceRunnable = object : Runnable {
        override fun run() {
            onSpecialKey?.invoke("backspace")
            handler?.postDelayed(this, 50)
        }
    }
    private fun startBackspaceRepeat() { handler?.postDelayed(backspaceRunnable, 400) }
    private fun stopBackspaceRepeat() { handler?.removeCallbacks(backspaceRunnable) }

    companion object {
        fun toDisplayChar(char: String): String {
            if (char.isEmpty()) return char
            val cp = char.codePointAt(0)
            val isCombining = cp == 0x0E31 ||
                    (cp in 0x0E34..0x0E3A) ||
                    (cp in 0x0E47..0x0E4E)
            return if (isCombining) "ก$char" else char
        }
    }
}
