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
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx

/**
 * Toolbar row above the suggestion bar, matching Gboard style.
 * Shows icons: Clipboard, Settings, Mic, Emoji, Search
 */
class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onClipboard: (() -> Unit)? = null
    var onSettings: (() -> Unit)? = null
    var onMic: (() -> Unit)? = null
    var onEmoji: (() -> Unit)? = null
    var onSearch: (() -> Unit)? = null

    private data class ToolbarItem(val icon: Drawable?, val action: () -> Unit)

    private var items = listOf<ToolbarItem>()
    private var pressedIndex = -1

    private inline val colors get() = ThemeManager.currentColors

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f.dpToPx(context)
    }
    private val cornerR = 8f.dpToPx(context)
    private val pad = 4f.dpToPx(context)
    private val rect = RectF()

    private val clipboardIcon = ContextCompat.getDrawable(context, R.drawable.ic_clipboard)
    private val settingsIcon = ContextCompat.getDrawable(context, R.drawable.ic_settings)
    private val micIcon = ContextCompat.getDrawable(context, R.drawable.ic_mic)
    private val emojiIcon = ContextCompat.getDrawable(context, R.drawable.ic_emoji)
    private val searchIcon = ContextCompat.getDrawable(context, R.drawable.ic_search)

    private fun buildItems() {
        items = listOfNotNull(
            ToolbarItem(searchIcon) { onSearch?.invoke() },
            ToolbarItem(emojiIcon) { onEmoji?.invoke() },
            ToolbarItem(clipboardIcon) { onClipboard?.invoke() },
            ToolbarItem(settingsIcon) { onSettings?.invoke() },
            ToolbarItem(micIcon) { onMic?.invoke() }
        )
    }

    init { buildItems() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(colors.kbBg)
        dividerPaint.color = colors.dividerColor

        if (items.isEmpty()) return
        val itemW = width.toFloat() / items.size
        val iconSize = (height * 0.45f).toInt()

        // Bottom divider line
        canvas.drawLine(0f, height.toFloat() - 1f, width.toFloat(), height.toFloat() - 1f, dividerPaint)

        for (i in items.indices) {
            val left = i * itemW + pad
            val top = pad
            val right = (i + 1) * itemW - pad
            val bottom = height.toFloat() - pad
            rect.set(left, top, right, bottom)

            if (i == pressedIndex) {
                bgPaint.color = colors.utilKeyPressed
                canvas.drawRoundRect(rect, cornerR, cornerR, bgPaint)
            }

            val cx = rect.centerX().toInt()
            val cy = rect.centerY().toInt()
            items[i].icon?.let {
                it.setBounds(cx - iconSize / 2, cy - iconSize / 2, cx + iconSize / 2, cy + iconSize / 2)
                it.setTint(colors.hintColor)
                it.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (items.isEmpty()) return false
        val itemW = width.toFloat() / items.size
        val index = (event.x / itemW).toInt().coerceIn(0, items.size - 1)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedIndex = index; invalidate(); return true
            }
            MotionEvent.ACTION_UP -> {
                if (index == pressedIndex && index in items.indices) items[index].action()
                pressedIndex = -1; invalidate(); return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1; invalidate(); return true
            }
        }
        return false
    }
}
