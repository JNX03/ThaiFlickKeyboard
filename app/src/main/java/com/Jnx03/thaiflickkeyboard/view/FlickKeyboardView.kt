package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
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
 * Flick keyboard matching Japanese flick keyboard style.
 *
 *   [🎤]  [k1] [k2] [k3]  [⌫]
 *   [◀]   [k4] [k5] [k6]  [▶]
 *   [123]  [k7] [k8] [k9]  [Space]
 *   [😀]  [k10][k11][k12]  [↵]
 */
class FlickKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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

    // Space key: tap=space, flick=tone marks
    private val toneSpaceKey = FlickKey("space_tone", "Space", "่", "์", "้", "๊", "", "")
    // Emoji key: tap=emoji, flick=special Thai chars
    private val emojiSpecialKey = FlickKey("emoji_special", "😀", "ๆ", "ฯ", "็", "๋", "", "")

    private val gestureDetector = FlickGestureDetector(20f.dpToPx(context))

    // Touch state
    private var activeCol = -1
    private var activeRow = -1
    private var currentDirection = FlickDirection.TAP
    private var isTouching = false

    // Grid
    private val COLS = 5
    private val ROWS = 4
    private val utilColRatio = 0.13f
    private val charColRatio = (1f - 2 * utilColRatio) / 3f

    private var colWidths = FloatArray(COLS)
    private var colStarts = FloatArray(COLS)
    private var rowHeight = 0f

    // Colors — matching dark Japanese flick keyboard
    private val kbBg = Color.parseColor("#1C1C1E")
    private val charKeyBg = Color.parseColor("#3A3A3C")
    private val charKeyPressed = Color.parseColor("#4285f4")
    private val utilKeyBg = Color.parseColor("#2C2C2E")
    private val utilKeyPressed = Color.parseColor("#3A3A3C")
    private val textColor = Color.parseColor("#FFFFFF")
    private val hintColor = Color.parseColor("#8E8E93")
    private val flickBalloonBg = Color.parseColor("#3A3A3C")
    private val flickBalloonActive = Color.parseColor("#4285f4")
    private val flickDimOverlay = Color.argb(160, 0, 0, 0)

    private val dimPaint = Paint().apply { color = flickDimOverlay }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor; textAlign = Paint.Align.CENTER
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = hintColor; textAlign = Paint.Align.CENTER
    }

    private val pad = 3f.dpToPx(context)
    private val cornerR = 10f.dpToPx(context)
    private val rect = RectF()
    private val flickRect = RectF()

    private val backspaceIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_backspace)
    private val enterIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_enter)
    private val micIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_mic)
    private val emojiIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_emoji)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (resources.displayMetrics.heightPixels * 0.32f).toInt()
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
        updateGestureExclusionRects()
    }

    private fun updateGestureExclusionRects() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val leftColRight = (colStarts[0] + colWidths[0]).toInt()
            val rightColLeft = colStarts[4].toInt()
            systemGestureExclusionRects = listOf(
                Rect(0, 0, leftColRight, height),
                Rect(rightColLeft, 0, width, height)
            )
        }
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
                if (col in 1..3) drawCharKey(canvas, row, col, rect, isActive)
                else drawUtilKey(canvas, row, col, rect, isActive)
            }
        }

        // When flicking: dim everything, then draw the active key + balloons on top
        if (isTouching && currentDirection != FlickDirection.TAP) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

            // Redraw the active key on top of the dim overlay
            val ax = colStarts[activeCol] + pad
            val ay = activeRow * rowHeight + pad
            val aw = colWidths[activeCol] - pad * 2
            val ah = rowHeight - pad * 2
            rect.set(ax, ay, ax + aw, ay + ah)
            if (activeCol in 1..3) drawCharKey(canvas, activeRow, activeCol, rect, true)
            else drawUtilKey(canvas, activeRow, activeCol, rect, true)

            drawFlickOverlay(canvas)
        }
    }

    private fun drawCharKey(canvas: Canvas, row: Int, col: Int, rect: RectF, isActive: Boolean) {
        val keyIndex = row * 3 + (col - 1)
        if (keyIndex >= charKeys.size) return
        val key = charKeys[keyIndex]

        // Background — blue highlight when active
        keyBgPaint.color = if (isActive) charKeyPressed else charKeyBg
        canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)

        val cx = rect.centerX()
        val cy = rect.centerY()

        // Main character (large, white)
        textPaint.textSize = 24f.spToPx(context)
        textPaint.color = textColor
        textPaint.isFakeBoldText = false
        canvas.drawText(toDisplayChar(key.tap), cx, cy + 8f.dpToPx(context), textPaint)

        // Flick hints at edges (small, gray) — only when not active
        if (!isActive) {
            hintPaint.textSize = 10f.spToPx(context)
            if (key.up.isNotEmpty())
                canvas.drawText(toDisplayChar(key.up), cx, rect.top + 14f.dpToPx(context), hintPaint)
            if (key.down.isNotEmpty())
                canvas.drawText(toDisplayChar(key.down), cx, rect.bottom - 5f.dpToPx(context), hintPaint)
            if (key.left.isNotEmpty())
                canvas.drawText(toDisplayChar(key.left), rect.left + 14f.dpToPx(context), cy + 4f.dpToPx(context), hintPaint)
            if (key.right.isNotEmpty())
                canvas.drawText(toDisplayChar(key.right), rect.right - 14f.dpToPx(context), cy + 4f.dpToPx(context), hintPaint)
        }
    }

    private fun drawUtilKey(canvas: Canvas, row: Int, col: Int, rect: RectF, isActive: Boolean) {
        keyBgPaint.color = if (isActive) utilKeyPressed else utilKeyBg
        canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)

        val cx = rect.centerX()
        val cy = rect.centerY()
        textPaint.color = textColor
        textPaint.isFakeBoldText = false

        when {
            col == 0 && row == 0 -> drawIcon(canvas, micIcon, rect, 0.35f)
            col == 0 && row == 1 -> {
                textPaint.textSize = 18f.spToPx(context)
                canvas.drawText("<", cx, cy + 6f.dpToPx(context), textPaint)
            }
            col == 0 && row == 2 -> {
                textPaint.textSize = 14f.spToPx(context)
                canvas.drawText(modeLabel, cx, cy + 5f.dpToPx(context), textPaint)
            }
            col == 0 && row == 3 -> drawIcon(canvas, emojiIcon, rect, 0.35f)
            col == 4 && row == 0 -> drawIcon(canvas, backspaceIcon, rect, 0.40f)
            col == 4 && row == 1 -> {
                textPaint.textSize = 18f.spToPx(context)
                canvas.drawText(">", cx, cy + 6f.dpToPx(context), textPaint)
            }
            col == 4 && row == 2 -> {
                // Space key with tone hints
                val spaceActive = isTouching && activeCol == col && activeRow == row
                if (spaceActive) {
                    keyBgPaint.color = charKeyPressed
                    canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)
                }
                textPaint.textSize = 12f.spToPx(context)
                canvas.drawText("Space", cx, cy + 4f.dpToPx(context), textPaint)
                if (!spaceActive) {
                    hintPaint.textSize = 8f.spToPx(context)
                    canvas.drawText(toDisplayChar(toneSpaceKey.left), rect.left + 8f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                    canvas.drawText(toDisplayChar(toneSpaceKey.up), cx, rect.top + 10f.dpToPx(context), hintPaint)
                    canvas.drawText(toDisplayChar(toneSpaceKey.right), rect.right - 8f.dpToPx(context), cy + 3f.dpToPx(context), hintPaint)
                    canvas.drawText(toDisplayChar(toneSpaceKey.down), cx, rect.bottom - 3f.dpToPx(context), hintPaint)
                }
            }
            col == 4 && row == 3 -> drawIcon(canvas, enterIcon, rect, 0.35f)
        }
    }

    /** Draw the inline flick balloons extending from the active key */
    private fun drawFlickOverlay(canvas: Canvas) {
        val flickKey = getFlickKeyAt(activeCol, activeRow) ?: return
        val keyX = colStarts[activeCol] + pad
        val keyY = activeRow * rowHeight + pad
        val keyW = colWidths[activeCol] - pad * 2
        val keyH = rowHeight - pad * 2
        val keyCx = keyX + keyW / 2
        val keyCy = keyY + keyH / 2

        val balloonW = keyW * 0.85f
        val balloonH = keyH * 0.75f

        // Draw each direction balloon
        drawBalloon(canvas, flickKey.up, FlickDirection.UP,
            keyCx - balloonW / 2, keyY - balloonH - pad, balloonW, balloonH)
        drawBalloon(canvas, flickKey.down, FlickDirection.DOWN,
            keyCx - balloonW / 2, keyY + keyH + pad, balloonW, balloonH)
        drawBalloon(canvas, flickKey.left, FlickDirection.LEFT,
            keyX - balloonW - pad, keyCy - balloonH / 2, balloonW, balloonH)
        drawBalloon(canvas, flickKey.right, FlickDirection.RIGHT,
            keyX + keyW + pad, keyCy - balloonH / 2, balloonW, balloonH)
    }

    private fun drawBalloon(canvas: Canvas, char: String, dir: FlickDirection,
                            x: Float, y: Float, w: Float, h: Float) {
        if (char.isEmpty()) return
        flickRect.set(x, y, x + w, y + h)

        val isActive = dir == currentDirection
        keyBgPaint.color = if (isActive) flickBalloonActive else flickBalloonBg
        canvas.drawRoundRect(flickRect, cornerR, cornerR, keyBgPaint)

        textPaint.textSize = 22f.spToPx(context)
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = isActive
        canvas.drawText(toDisplayChar(char), flickRect.centerX(),
            flickRect.centerY() + 8f.dpToPx(context), textPaint)
        textPaint.isFakeBoldText = false
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

                if (col == 4 && row == 0) {
                    onSpecialKey?.invoke("backspace")
                    startBackspaceRepeat()
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
                            invalidate()
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                stopBackspaceRepeat()
                if (isTouching) {
                    val direction = gestureDetector.onTouchUp()
                    handleKeyUp(activeCol, activeRow, direction)
                }
                activeCol = -1
                activeRow = -1
                isTouching = false
                currentDirection = FlickDirection.TAP
                gestureDetector.reset()
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                stopBackspaceRepeat()
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
        // Space: tap=space, flick=tones
        if (col == 4 && row == 2) {
            if (direction == FlickDirection.TAP) onSpace?.invoke()
            else {
                val c = toneSpaceKey.charForDirection(direction)
                if (c.isNotEmpty() && c != "Space") onCharacterSelected?.invoke(c)
            }
            return
        }
        // Emoji: tap=emoji, flick=special
        if (col == 0 && row == 3) {
            if (direction == FlickDirection.TAP) onEmojiPressed?.invoke()
            else {
                val c = emojiSpecialKey.charForDirection(direction)
                if (c.isNotEmpty()) onCharacterSelected?.invoke(c)
            }
            return
        }
        // Character keys
        val flickKey = getFlickKeyAt(col, row)
        if (flickKey != null) {
            val c = flickKey.charForDirection(direction)
            if (c.isNotEmpty()) onCharacterSelected?.invoke(c)
            return
        }
        // Utility keys
        when {
            col == 0 && row == 0 -> onMicPressed?.invoke()
            col == 0 && row == 1 -> onCursorLeft?.invoke()
            col == 0 && row == 2 -> onSpecialKey?.invoke("mode")
            col == 4 && row == 0 -> { /* backspace handled on DOWN */ }
            col == 4 && row == 1 -> onCursorRight?.invoke()
            col == 4 && row == 3 -> onEnter?.invoke()
        }
    }

    private fun getFlickKeyAt(col: Int, row: Int): FlickKey? {
        if (col in 1..3) return charKeys.getOrNull(row * 3 + (col - 1))
        if (col == 4 && row == 2) return toneSpaceKey
        if (col == 0 && row == 3) return emojiSpecialKey
        return null
    }

    private fun getCol(x: Float): Int {
        for (i in 0 until COLS) if (x >= colStarts[i] && x < colStarts[i] + colWidths[i]) return i
        return -1
    }

    private fun getRow(y: Float): Int {
        val r = (y / rowHeight).toInt()
        return if (r in 0 until ROWS) r else -1
    }

    private val backspaceRunnable = object : Runnable {
        override fun run() { onSpecialKey?.invoke("backspace"); handler?.postDelayed(this, 50) }
    }
    private fun startBackspaceRepeat() { handler?.postDelayed(backspaceRunnable, 400) }
    private fun stopBackspaceRepeat() { handler?.removeCallbacks(backspaceRunnable) }

    companion object {
        fun toDisplayChar(char: String): String {
            if (char.isEmpty()) return char
            val cp = char.codePointAt(0)
            val isCombining = cp == 0x0E31 || (cp in 0x0E34..0x0E3A) || (cp in 0x0E47..0x0E4E)
            return if (isCombining) "ก$char" else char
        }
    }
}
