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
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

/**
 * Standard QWERTY keyboard for English input.
 */
class QwertyKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCharacterSelected: ((String) -> Unit)? = null
    var onBackspace: (() -> Unit)? = null
    var onSpace: (() -> Unit)? = null
    var onEnter: (() -> Unit)? = null
    var onSwitchMode: (() -> Unit)? = null
    var hapticEnabled: Boolean = true

    private var isShifted = false
    private var isCaps = false

    private val rows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("SHIFT","z","x","c","v","b","n","m","BKSP"),
        listOf("MODE",",","SPACE",".","ENTER")
    )

    private inline val colors get() = ThemeManager.currentColors

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val pad = 3f.dpToPx(context)
    private val cornerR = 8f.dpToPx(context)
    private val rect = RectF()

    private val backspaceIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_backspace)
    private val enterIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_enter)

    // Touch state
    private var activeRow = -1
    private var activeKeyIdx = -1
    private var isTouching = false

    // Layout cache
    private var keyRects = Array(4) { arrayOf<RectF>() }
    private var rowHeight = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (resources.displayMetrics.heightPixels * 0.32f).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rowHeight = h.toFloat() / rows.size
        val baseKeyW = w.toFloat() / 10f

        keyRects = Array(rows.size) { rowIdx ->
            val row = rows[rowIdx]
            when (rowIdx) {
                0 -> { // 10 equal keys
                    Array(10) { i ->
                        RectF(i * baseKeyW + pad, rowIdx * rowHeight + pad,
                            (i + 1) * baseKeyW - pad, (rowIdx + 1) * rowHeight - pad)
                    }
                }
                1 -> { // 9 keys, centered with half-key offset
                    val offset = baseKeyW * 0.5f
                    Array(9) { i ->
                        RectF(offset + i * baseKeyW + pad, rowIdx * rowHeight + pad,
                            offset + (i + 1) * baseKeyW - pad, (rowIdx + 1) * rowHeight - pad)
                    }
                }
                2 -> { // shift + 7 chars + backspace
                    val shiftW = baseKeyW * 1.5f
                    val bkspW = baseKeyW * 1.5f
                    val charW = (w - shiftW - bkspW) / 7f
                    Array(9) { i ->
                        when (i) {
                            0 -> RectF(pad, rowIdx * rowHeight + pad,
                                shiftW - pad, (rowIdx + 1) * rowHeight - pad)
                            8 -> RectF(w - bkspW + pad, rowIdx * rowHeight + pad,
                                w.toFloat() - pad, (rowIdx + 1) * rowHeight - pad)
                            else -> RectF(shiftW + (i - 1) * charW + pad, rowIdx * rowHeight + pad,
                                shiftW + i * charW - pad, (rowIdx + 1) * rowHeight - pad)
                        }
                    }
                }
                3 -> { // mode, comma, space, period, enter
                    val modeW = baseKeyW * 1.5f
                    val enterW = baseKeyW * 1.5f
                    val commaW = baseKeyW
                    val dotW = baseKeyW
                    val spaceW = w - modeW - enterW - commaW - dotW
                    var x = 0f
                    Array(5) { i ->
                        val kw = when (i) { 0 -> modeW; 1 -> commaW; 2 -> spaceW; 3 -> dotW; else -> enterW }
                        val r = RectF(x + pad, rowIdx * rowHeight + pad, x + kw - pad, (rowIdx + 1) * rowHeight - pad)
                        x += kw
                        r
                    }
                }
                else -> arrayOf()
            }
        }
        updateGestureExclusionRects()
    }

    private fun updateGestureExclusionRects() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val edgeWidth = (20f.dpToPx(context)).toInt()
            systemGestureExclusionRects = listOf(
                Rect(0, 0, edgeWidth, height),
                Rect(width - edgeWidth, 0, width, height)
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(colors.kbBg)
        textPaint.color = colors.textColor

        for (rowIdx in rows.indices) {
            val row = rows[rowIdx]
            for (keyIdx in row.indices) {
                if (rowIdx >= keyRects.size || keyIdx >= keyRects[rowIdx].size) continue
                val r = keyRects[rowIdx][keyIdx]
                val key = row[keyIdx]
                val isActive = isTouching && activeRow == rowIdx && activeKeyIdx == keyIdx

                val isUtil = key == "SHIFT" || key == "BKSP" || key == "MODE" || key == "ENTER"
                keyBgPaint.color = when {
                    isActive -> colors.charKeyPressed
                    isUtil -> colors.utilKeyBg
                    else -> colors.charKeyBg
                }
                canvas.drawRoundRect(r, cornerR, cornerR, keyBgPaint)

                val cx = r.centerX()
                val cy = r.centerY()

                when (key) {
                    "SHIFT" -> {
                        textPaint.textSize = 16f.spToPx(context)
                        textPaint.color = if (isShifted || isCaps) colors.charKeyPressed else colors.textColor
                        canvas.drawText(if (isCaps) "⇪" else "⇧", cx, cy + 6f.dpToPx(context), textPaint)
                        textPaint.color = colors.textColor
                    }
                    "BKSP" -> drawIcon(canvas, backspaceIcon, r, 0.35f)
                    "ENTER" -> drawIcon(canvas, enterIcon, r, 0.35f)
                    "MODE" -> {
                        textPaint.textSize = 13f.spToPx(context)
                        canvas.drawText("ก", cx, cy + 5f.dpToPx(context), textPaint)
                    }
                    "SPACE" -> {
                        textPaint.textSize = 12f.spToPx(context)
                        canvas.drawText("space", cx, cy + 4f.dpToPx(context), textPaint)
                    }
                    else -> {
                        textPaint.textSize = 20f.spToPx(context)
                        val display = if (isShifted || isCaps) key.uppercase() else key
                        canvas.drawText(display, cx, cy + 7f.dpToPx(context), textPaint)
                    }
                }
            }
        }
    }

    private fun drawIcon(canvas: Canvas, icon: Drawable?, rect: RectF, scale: Float) {
        icon?.let {
            val size = (rect.height() * scale).toInt()
            val cx = rect.centerX().toInt()
            val cy = rect.centerY().toInt()
            it.setBounds(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
            it.setTint(colors.textColor)
            it.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                findKey(event.x, event.y)?.let { (r, k) ->
                    activeRow = r; activeKeyIdx = k; isTouching = true
                    if (rows[r][k] == "BKSP") { onBackspace?.invoke(); startBackspaceRepeat() }
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                stopBackspaceRepeat()
                if (isTouching && activeRow >= 0) {
                    val key = rows[activeRow][activeKeyIdx]
                    when (key) {
                        "SHIFT" -> {
                            if (isShifted) { isCaps = !isCaps; isShifted = isCaps }
                            else { isShifted = true; isCaps = false }
                        }
                        "BKSP" -> { /* handled on DOWN */ }
                        "ENTER" -> onEnter?.invoke()
                        "MODE" -> onSwitchMode?.invoke()
                        "SPACE" -> onSpace?.invoke()
                        else -> {
                            val ch = if (isShifted || isCaps) key.uppercase() else key
                            onCharacterSelected?.invoke(ch)
                            if (isShifted && !isCaps) isShifted = false
                        }
                    }
                }
                activeRow = -1; activeKeyIdx = -1; isTouching = false
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                stopBackspaceRepeat()
                activeRow = -1; activeKeyIdx = -1; isTouching = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKey(x: Float, y: Float): Pair<Int, Int>? {
        for (rowIdx in rows.indices) {
            if (rowIdx >= keyRects.size) continue
            for (keyIdx in keyRects[rowIdx].indices) {
                if (keyRects[rowIdx][keyIdx].contains(x, y)) return Pair(rowIdx, keyIdx)
            }
        }
        return null
    }

    private val backspaceRunnable = object : Runnable {
        override fun run() { onBackspace?.invoke(); handler?.postDelayed(this, 50) }
    }
    private fun startBackspaceRepeat() { handler?.postDelayed(backspaceRunnable, 400) }
    private fun stopBackspaceRepeat() { handler?.removeCallbacks(backspaceRunnable) }
}
