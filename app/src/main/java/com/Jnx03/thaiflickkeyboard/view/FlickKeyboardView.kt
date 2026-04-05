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

    private var activeKeyIndex = -1
    private var currentDirection = FlickDirection.TAP
    private var isTouching = false

    private val previewPopup = FlickPreviewPopup(context)

    // Paints
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyActiveBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val primaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 22f.spToPx(context)
    }
    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = 11f.spToPx(context)
    }
    private val activeSecondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 14f.spToPx(context)
    }
    private val hintTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#60FFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = 8f.spToPx(context)
    }
    private val flickLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40FFFFFF")
        strokeWidth = 2f.dpToPx(context)
        style = Paint.Style.STROKE
    }

    private val keyPadding = 2f.dpToPx(context)
    private val cornerRadius = 8f.dpToPx(context)
    private val keyRect = RectF()

    private var cellWidth = 0f
    private var cellHeight = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = resources.displayMetrics.heightPixels
        val height = (screenHeight * 0.33f).toInt()
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

        // Background
        val baseColor = try {
            Color.parseColor(key.color)
        } catch (e: Exception) {
            Color.GRAY
        }

        if (isActive) {
            keyActiveBgPaint.color = baseColor
            keyActiveBgPaint.alpha = 120
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyActiveBgPaint)
        } else {
            keyBgPaint.color = baseColor
            keyBgPaint.alpha = 50
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBgPaint)
        }

        if (isSpecialKey) {
            // Draw special key with just the primary text
            primaryTextPaint.textSize = 18f.spToPx(context)
            canvas.drawText(key.tap, cx, cy + primaryTextPaint.textSize / 3, primaryTextPaint)
            primaryTextPaint.textSize = 22f.spToPx(context)
            return
        }

        val offsetX = rect.width() * 0.30f
        val offsetY = rect.height() * 0.28f

        // Draw directional characters
        if (isActive && currentDirection != FlickDirection.TAP) {
            // Dim non-active directions, highlight active one
            drawDirectionalChars(canvas, key, cx, cy, offsetX, offsetY, currentDirection)

            // Draw flick line
            val targetX = when (currentDirection) {
                FlickDirection.LEFT -> cx - offsetX
                FlickDirection.RIGHT -> cx + offsetX
                else -> cx
            }
            val targetY = when (currentDirection) {
                FlickDirection.UP -> cy - offsetY
                FlickDirection.DOWN -> cy + offsetY
                else -> cy
            }
            canvas.drawLine(cx, cy, targetX, targetY, flickLinePaint)
        } else {
            // Normal state: all directional chars at secondary opacity
            drawDirectionalCharsNormal(canvas, key, cx, cy, offsetX, offsetY)
        }

        // Center (tap) character - always draw
        val tapPaint = if (isActive && currentDirection == FlickDirection.TAP) {
            activeSecondaryPaint.apply { textSize = 24f.spToPx(context) }
        } else {
            primaryTextPaint
        }
        canvas.drawText(key.tap, cx, cy + tapPaint.textSize / 3, tapPaint)

        // Hint text at bottom
        if (!isActive) {
            canvas.drawText(key.hint, cx, rect.bottom - keyPadding * 2, hintTextPaint)
        }
    }

    private fun drawDirectionalCharsNormal(
        canvas: Canvas, key: FlickKey,
        cx: Float, cy: Float, offsetX: Float, offsetY: Float
    ) {
        if (key.left.isNotEmpty())
            canvas.drawText(key.left, cx - offsetX, cy + secondaryTextPaint.textSize / 3, secondaryTextPaint)
        if (key.right.isNotEmpty())
            canvas.drawText(key.right, cx + offsetX, cy + secondaryTextPaint.textSize / 3, secondaryTextPaint)
        if (key.up.isNotEmpty())
            canvas.drawText(key.up, cx, cy - offsetY + secondaryTextPaint.textSize / 3, secondaryTextPaint)
        if (key.down.isNotEmpty())
            canvas.drawText(key.down, cx, cy + offsetY + secondaryTextPaint.textSize / 3, secondaryTextPaint)
    }

    private fun drawDirectionalChars(
        canvas: Canvas, key: FlickKey,
        cx: Float, cy: Float, offsetX: Float, offsetY: Float,
        activeDir: FlickDirection
    ) {
        val dimPaint = Paint(secondaryTextPaint).apply { alpha = 60 }

        val leftPaint = if (activeDir == FlickDirection.LEFT) activeSecondaryPaint else dimPaint
        val rightPaint = if (activeDir == FlickDirection.RIGHT) activeSecondaryPaint else dimPaint
        val upPaint = if (activeDir == FlickDirection.UP) activeSecondaryPaint else dimPaint
        val downPaint = if (activeDir == FlickDirection.DOWN) activeSecondaryPaint else dimPaint

        if (key.left.isNotEmpty())
            canvas.drawText(key.left, cx - offsetX, cy + leftPaint.textSize / 3, leftPaint)
        if (key.right.isNotEmpty())
            canvas.drawText(key.right, cx + offsetX, cy + rightPaint.textSize / 3, rightPaint)
        if (key.up.isNotEmpty())
            canvas.drawText(key.up, cx, cy - offsetY + upPaint.textSize / 3, upPaint)
        if (key.down.isNotEmpty())
            canvas.drawText(key.down, cx, cy + offsetY + downPaint.textSize / 3, downPaint)
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
                            updatePreview(key, newDir, event)
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
                previewPopup.dismiss()
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

    private fun getKeyIndexAt(x: Float, y: Float): Int {
        if (cellWidth <= 0 || cellHeight <= 0) return -1
        val col = (x / cellWidth).toInt()
        val row = (y / cellHeight).toInt()
        if (col < 0 || col >= layout.columns || row < 0 || row >= layout.rows) return -1
        val index = row * layout.columns + col
        return if (index in layout.keys.indices) index else -1
    }

    private fun updatePreview(key: FlickKey, direction: FlickDirection, event: MotionEvent) {
        val char = key.charForDirection(direction)
        if (char.isNotEmpty() && key.id != "backspace" && key.id != "mode") {
            val color = try {
                Color.parseColor(key.color)
            } catch (e: Exception) {
                Color.GRAY
            }
            val location = IntArray(2)
            getLocationOnScreen(location)
            val screenX = location[0] + event.x.toInt()
            val screenY = location[1] + (activeKeyIndex / layout.columns) * cellHeight.toInt() - 80
            previewPopup.show(this, char, color, screenX, screenY.toInt())
        }
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

    fun updateSensitivity(radiusDp: Float) {
        val newDetector = FlickGestureDetector(radiusDp.dpToPx(context))
        // We can't replace gestureDetector directly since it's val, but we can work around:
        // For simplicity, we'll handle this in the service by recreating the view
    }
}
