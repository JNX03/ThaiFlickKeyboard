package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.Jnx03.thaiflickkeyboard.gesture.FlickGestureDetector
import com.Jnx03.thaiflickkeyboard.model.FlickDirection
import com.Jnx03.thaiflickkeyboard.model.FlickKey
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class FlickKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var layout: KeyboardLayout = KeyboardLayout.default()
        set(value) {
            field = value
            invalidate()
        }

    var onCharacterSelected: ((String) -> Unit)? = null
    var onSpecialKey: ((String) -> Unit)? = null
    var hapticEnabled: Boolean = true

    private val gestureDetector = FlickGestureDetector(20f.dpToPx(context))
    private val crossPopup = FlickCrossPopup(context)

    private var activeKeyIndex = -1
    private var currentDirection = FlickDirection.TAP
    private var isTouching = false

    // Gboard dark theme colors
    private val keyBgColor = Color.parseColor("#3c3c3c")
    private val keyPressedColor = Color.parseColor("#4a4a4a")
    private val textColor = Color.parseColor("#e8e8e8")
    private val hintColor = Color.parseColor("#666666")
    private val categoryBarHeight = 3f.dpToPx(context)

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val primaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = 24f.spToPx(context)
    }
    private val hintTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = hintColor
        textAlign = Paint.Align.CENTER
        textSize = 10f.spToPx(context)
    }
    private val categoryPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val keyPadding = 3f.dpToPx(context)
    private val cornerRadius = 10f.dpToPx(context)
    private val keyRect = RectF()

    private var cellWidth = 0f
    private var cellHeight = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = resources.displayMetrics.heightPixels
        val height = (screenHeight * 0.32f).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = w.toFloat() / layout.columns
        cellHeight = h.toFloat() / layout.rows
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellWidth = width.toFloat() / layout.columns
        cellHeight = height.toFloat() / layout.rows

        for (i in layout.keys.indices) {
            val key = layout.keys[i]
            val col = i % layout.columns
            val row = i / layout.columns

            val left = col * cellWidth + keyPadding
            val top = row * cellHeight + keyPadding
            val right = (col + 1) * cellWidth - keyPadding
            val bottom = (row + 1) * cellHeight - keyPadding

            keyRect.set(left, top, right, bottom)
            drawKey(canvas, key, keyRect, i)
        }
    }

    private fun drawKey(canvas: Canvas, key: FlickKey, rect: RectF, index: Int) {
        val isActive = isTouching && index == activeKeyIndex
        val cx = rect.centerX()
        val cy = rect.centerY()
        val isSpecialKey = key.id == "backspace" || key.id == "mode"

        // Key background
        keyBgPaint.color = if (isActive) keyPressedColor else keyBgColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBgPaint)

        // Category color bar at top of key
        if (!isSpecialKey && key.id != "tone" && key.id != "special") {
            try {
                categoryPaint.color = Color.parseColor(key.color)
                categoryPaint.alpha = 180
                val barRect = RectF(rect.left + cornerRadius, rect.top,
                    rect.right - cornerRadius, rect.top + categoryBarHeight)
                canvas.drawRect(barRect, categoryPaint)
            } catch (_: Exception) {}
        }

        // Main tap character (large, centered)
        val displayTap = toDisplayChar(key.tap)
        primaryTextPaint.textSize = if (isSpecialKey) 18f.spToPx(context) else 24f.spToPx(context)
        canvas.drawText(displayTap, cx, cy + primaryTextPaint.textSize / 3, primaryTextPaint)

        // Direction hints (small, at edges) - only for non-special keys
        if (!isSpecialKey && !isActive) {
            val offsetX = rect.width() * 0.32f
            val offsetY = rect.height() * 0.32f
            val hintY = hintTextPaint.textSize / 3

            if (key.left.isNotEmpty())
                canvas.drawText(toDisplayChar(key.left), cx - offsetX, cy + hintY, hintTextPaint)
            if (key.right.isNotEmpty())
                canvas.drawText(toDisplayChar(key.right), cx + offsetX, cy + hintY, hintTextPaint)
            if (key.up.isNotEmpty())
                canvas.drawText(toDisplayChar(key.up), cx, cy - offsetY + hintY, hintTextPaint)
            if (key.down.isNotEmpty())
                canvas.drawText(toDisplayChar(key.down), cx, cy + offsetY + hintY, hintTextPaint)
        }

        // Hint label at bottom
        if (!isActive && key.hint.isNotEmpty() && !isSpecialKey) {
            hintTextPaint.textSize = 8f.spToPx(context)
            canvas.drawText(key.hint, cx, rect.bottom - keyPadding * 2, hintTextPaint)
            hintTextPaint.textSize = 10f.spToPx(context)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val keyIndex = getKeyIndexAt(event.x, event.y)
                if (keyIndex >= 0) {
                    activeKeyIndex = keyIndex
                    isTouching = true
                    currentDirection = FlickDirection.TAP
                    gestureDetector.onTouchDown(event.x, event.y)
                    invalidate()

                    val key = layout.keys[keyIndex]
                    if (key.id == "backspace") {
                        onSpecialKey?.invoke("backspace")
                        startBackspaceRepeat()
                    } else if (key.id != "mode") {
                        showCrossPopup(key, keyIndex)
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouching && activeKeyIndex >= 0) {
                    val key = layout.keys[activeKeyIndex]
                    if (key.id != "backspace" && key.id != "mode") {
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
                if (isTouching && activeKeyIndex >= 0) {
                    val key = layout.keys[activeKeyIndex]
                    val direction = gestureDetector.onTouchUp()

                    when (key.id) {
                        "backspace" -> { /* already handled */ }
                        "mode" -> onSpecialKey?.invoke("mode")
                        else -> {
                            val char = key.charForDirection(direction)
                            if (char.isNotEmpty()) {
                                onCharacterSelected?.invoke(char)
                            }
                        }
                    }
                }
                crossPopup.dismiss()
                activeKeyIndex = -1
                isTouching = false
                currentDirection = FlickDirection.TAP
                gestureDetector.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun showCrossPopup(key: FlickKey, keyIndex: Int) {
        val col = keyIndex % layout.columns
        val row = keyIndex / layout.columns
        val location = IntArray(2)
        getLocationOnScreen(location)
        val anchorX = location[0] + ((col + 0.5f) * cellWidth).toInt()
        val anchorY = location[1] + (row * cellHeight).toInt()
        crossPopup.show(this, key, FlickDirection.TAP, anchorX, anchorY)
    }

    private fun getKeyIndexAt(x: Float, y: Float): Int {
        if (cellWidth <= 0 || cellHeight <= 0) return -1
        val col = (x / cellWidth).toInt()
        val row = (y / cellHeight).toInt()
        if (col < 0 || col >= layout.columns || row < 0 || row >= layout.rows) return -1
        val index = row * layout.columns + col
        return if (index in layout.keys.indices) index else -1
    }

    // Backspace repeat
    private val backspaceRunnable = object : Runnable {
        override fun run() {
            onSpecialKey?.invoke("backspace")
            handler?.postDelayed(this, 50)
        }
    }

    private fun startBackspaceRepeat() {
        handler?.postDelayed(backspaceRunnable, 400)
    }

    private fun stopBackspaceRepeat() {
        handler?.removeCallbacks(backspaceRunnable)
    }

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
