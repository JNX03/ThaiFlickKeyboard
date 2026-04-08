package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class EmojiCategoryBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCategorySelected: ((Int) -> Unit)? = null
    var selectedIndex: Int = 0
        set(value) { field = value; invalidate() }

    var categoryIcons: List<String> = emptyList()
        set(value) { field = value; invalidate() }

    private var pressedIndex = -1
    private inline val colors get() = ThemeManager.currentColors

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val indicatorR = 3f.dpToPx(context)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(colors.kbBg)

        // Top divider
        dividerPaint.color = colors.dividerColor
        canvas.drawRect(0f, 0f, width.toFloat(), 1f, dividerPaint)

        if (categoryIcons.isEmpty()) return

        val itemW = width.toFloat() / categoryIcons.size
        textPaint.textSize = 20f.spToPx(context)

        for (i in categoryIcons.indices) {
            val cx = i * itemW + itemW / 2
            val cy = height / 2f

            // Pressed highlight
            if (i == pressedIndex) {
                rect.set(i * itemW + 4f, 4f, (i + 1) * itemW - 4f, height.toFloat() - 4f)
                bgPaint.color = colors.utilKeyPressed
                canvas.drawRoundRect(rect, 8f.dpToPx(context), 8f.dpToPx(context), bgPaint)
            }

            // Emoji icon
            textPaint.color = if (i == selectedIndex) colors.textColor else colors.hintColor
            canvas.drawText(categoryIcons[i], cx, cy + 7f.dpToPx(context), textPaint)

            // Selected indicator dot
            if (i == selectedIndex) {
                indicatorPaint.color = colors.charKeyPressed
                canvas.drawRoundRect(
                    cx - 8f.dpToPx(context), height - 4f.dpToPx(context),
                    cx + 8f.dpToPx(context), height - 1f.dpToPx(context),
                    indicatorR, indicatorR, indicatorPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (categoryIcons.isEmpty()) return false
        val itemW = width.toFloat() / categoryIcons.size
        val index = (event.x / itemW).toInt().coerceIn(0, categoryIcons.size - 1)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedIndex = index; invalidate(); return true
            }
            MotionEvent.ACTION_UP -> {
                if (index == pressedIndex && index in categoryIcons.indices) {
                    selectedIndex = index
                    onCategorySelected?.invoke(index)
                }
                pressedIndex = -1; invalidate(); return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1; invalidate(); return true
            }
        }
        return false
    }
}
