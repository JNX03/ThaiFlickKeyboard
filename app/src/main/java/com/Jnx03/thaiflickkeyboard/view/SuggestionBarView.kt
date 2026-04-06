package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class SuggestionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSuggestionSelected: ((String) -> Unit)? = null

    private var suggestions = listOf<String>()
    private var pressedIndex = -1

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = 15f.spToPx(context)
    }
    private val pressedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A3A3C")
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A3A3C")
        strokeWidth = 1f.dpToPx(context)
    }

    // Flick balloon overlay state
    private var flickBalloonChar: String? = null
    private var flickBalloonCenterX = 0f
    private var flickBalloonWidth = 0f
    private var flickBalloonHeight = 0f
    private var flickBalloonActive = false
    private val flickBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flickTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 22f.spToPx(context)
    }
    private val flickRect = RectF()
    private val flickCornerR = 10f.dpToPx(context)
    private val flickPad = 3f.dpToPx(context)

    fun setSuggestions(words: List<String>) {
        suggestions = words.take(3)
        invalidate()
    }

    fun showFlickBalloon(displayChar: String?, centerX: Float, width: Float, balloonH: Float, isActive: Boolean) {
        if (displayChar == null) {
            if (flickBalloonChar != null) {
                flickBalloonChar = null
                invalidate()
            }
            return
        }
        flickBalloonChar = displayChar
        flickBalloonCenterX = centerX
        flickBalloonWidth = width
        flickBalloonHeight = balloonH
        flickBalloonActive = isActive
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val balloonChar = flickBalloonChar
        if (balloonChar != null) {
            drawFlickBalloonOverlay(canvas, balloonChar)
            return
        }

        if (suggestions.isEmpty()) return

        val cellWidth = width.toFloat() / suggestions.size

        for (i in suggestions.indices) {
            val left = i * cellWidth
            val right = (i + 1) * cellWidth

            if (i == pressedIndex) {
                canvas.drawRect(left, 0f, right, height.toFloat(), pressedBgPaint)
            }

            canvas.drawText(
                suggestions[i],
                left + cellWidth / 2,
                height / 2f + textPaint.textSize / 3,
                textPaint
            )

            if (i < suggestions.size - 1) {
                canvas.drawLine(right, height * 0.2f, right, height * 0.8f, dividerPaint)
            }
        }
    }

    private fun drawFlickBalloonOverlay(canvas: Canvas, displayChar: String) {
        val balloonH = if (flickBalloonHeight > 0f) flickBalloonHeight else (height.toFloat() - flickPad * 2)
        val balloonW = flickBalloonWidth
        val left = flickBalloonCenterX - balloonW / 2
        val top = height.toFloat() - balloonH - flickPad
        flickRect.set(left, top, left + balloonW, top + balloonH)

        flickBgPaint.color = if (flickBalloonActive) Color.parseColor("#4285f4") else Color.parseColor("#3A3A3C")
        canvas.drawRoundRect(flickRect, flickCornerR, flickCornerR, flickBgPaint)

        flickTextPaint.isFakeBoldText = flickBalloonActive
        canvas.drawText(displayChar, flickRect.centerX(),
            flickRect.centerY() + flickTextPaint.textSize / 3, flickTextPaint)
        flickTextPaint.isFakeBoldText = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (suggestions.isEmpty()) return false
        val cellWidth = width.toFloat() / suggestions.size
        val index = (event.x / cellWidth).toInt().coerceIn(0, suggestions.size - 1)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedIndex = index
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (index == pressedIndex && index in suggestions.indices) {
                    onSuggestionSelected?.invoke(suggestions[index])
                }
                pressedIndex = -1
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1
                invalidate()
                return true
            }
        }
        return false
    }
}
