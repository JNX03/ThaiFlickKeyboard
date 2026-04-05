package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.Jnx03.thaiflickkeyboard.data.ClipboardHistoryManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

/**
 * Clipboard history panel that replaces the keyboard temporarily.
 * Shows a scrollable list of clipboard entries with tap-to-paste.
 */
class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onPaste: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onDelete: ((Int) -> Unit)? = null
    var onClearAll: (() -> Unit)? = null

    private var items = listOf<ClipboardHistoryManager.ClipEntry>()
    private var scrollOffset = 0f
    private var lastTouchY = 0f
    private var pressedIndex = -1

    private val itemHeight = 52f.dpToPx(context)
    private val headerHeight = 44f.dpToPx(context)
    private val pad = 12f.dpToPx(context)
    private val cornerR = 8f.dpToPx(context)

    private val bgColor = Color.parseColor("#1C1C1E")
    private val itemBg = Color.parseColor("#2C2C2E")
    private val itemPressed = Color.parseColor("#3A3A3C")
    private val headerColor = Color.parseColor("#8E8E93")
    private val textColor = Color.parseColor("#FFFFFF")
    private val subtextColor = Color.parseColor("#8E8E93")
    private val deleteColor = Color.parseColor("#FF453A")
    private val clearBg = Color.parseColor("#2C2C2E")

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor; textSize = 15f.spToPx(context)
    }
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = headerColor; textSize = 13f.spToPx(context); textAlign = Paint.Align.LEFT
    }
    private val deletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = deleteColor; textSize = 13f.spToPx(context); textAlign = Paint.Align.CENTER
    }
    private val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = subtextColor; textSize = 11f.spToPx(context)
    }
    private val rect = RectF()

    fun setHistory(history: List<ClipboardHistoryManager.ClipEntry>) {
        items = history
        scrollOffset = 0f
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (resources.displayMetrics.heightPixels * 0.32f).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bgColor)

        // Header: "Clipboard" + Close button + Clear All
        bgPaint.color = bgColor
        headerPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Clipboard", pad, headerHeight / 2 + 5f.dpToPx(context), headerPaint)

        // Close button (X)
        headerPaint.textAlign = Paint.Align.RIGHT
        headerPaint.color = textColor
        canvas.drawText("✕", width - pad, headerHeight / 2 + 5f.dpToPx(context), headerPaint)
        headerPaint.color = headerColor

        // Clear all button
        if (items.isNotEmpty()) {
            deletePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Clear all", width - 40f.dpToPx(context), headerHeight / 2 + 5f.dpToPx(context), deletePaint)
        }

        // Divider
        bgPaint.color = Color.parseColor("#3A3A3C")
        canvas.drawRect(pad, headerHeight, width - pad, headerHeight + 1f, bgPaint)

        if (items.isEmpty()) {
            headerPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("No clipboard history", width / 2f, height / 2f, headerPaint)
            return
        }

        // Clip items
        canvas.save()
        canvas.clipRect(0f, headerHeight + 1f, width.toFloat(), height.toFloat())

        for (i in items.indices) {
            val y = headerHeight + 4f.dpToPx(context) + i * (itemHeight + 4f.dpToPx(context)) - scrollOffset
            if (y + itemHeight < headerHeight || y > height) continue

            rect.set(pad, y, width - pad, y + itemHeight)
            bgPaint.color = if (i == pressedIndex) itemPressed else itemBg
            canvas.drawRoundRect(rect, cornerR, cornerR, bgPaint)

            // Text (truncated)
            val text = items[i].text.replace("\n", " ")
            val maxChars = ((width - pad * 4) / textPaint.textSize * 1.7f).toInt()
            val display = if (text.length > maxChars) text.take(maxChars) + "…" else text
            canvas.drawText(display, pad * 2, y + itemHeight / 2 + 5f.dpToPx(context), textPaint)

            // Delete X on right
            deletePaint.textAlign = Paint.Align.CENTER
            canvas.drawText("✕", width - pad * 2.5f, y + itemHeight / 2 + 5f.dpToPx(context), deletePaint)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                pressedIndex = getItemAt(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = lastTouchY - event.y
                val maxScroll = maxOf(0f, items.size * (itemHeight + 4f.dpToPx(context)) - (height - headerHeight - 8f.dpToPx(context)))
                scrollOffset = (scrollOffset + dy).coerceIn(0f, maxScroll)
                lastTouchY = event.y
                pressedIndex = -1
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val idx = getItemAt(event.x, event.y)

                // Close button
                if (event.y < headerHeight && event.x > width - 40f.dpToPx(context)) {
                    onClose?.invoke()
                    pressedIndex = -1; invalidate(); return true
                }
                // Clear all button
                if (event.y < headerHeight && event.x > width - 120f.dpToPx(context) && items.isNotEmpty()) {
                    onClearAll?.invoke()
                    pressedIndex = -1; invalidate(); return true
                }
                // Delete button on item
                if (idx >= 0 && event.x > width - pad * 4) {
                    onDelete?.invoke(idx)
                    pressedIndex = -1; invalidate(); return true
                }
                // Tap item to paste
                if (idx >= 0 && idx == pressedIndex && idx in items.indices) {
                    onPaste?.invoke(items[idx].text)
                }
                pressedIndex = -1; invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1; invalidate(); return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getItemAt(x: Float, y: Float): Int {
        if (y < headerHeight) return -1
        for (i in items.indices) {
            val iy = headerHeight + 4f.dpToPx(context) + i * (itemHeight + 4f.dpToPx(context)) - scrollOffset
            if (y >= iy && y < iy + itemHeight) return i
        }
        return -1
    }
}
