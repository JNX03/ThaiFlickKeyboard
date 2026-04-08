package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class KeyboardHeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private inline val colors get() = ThemeManager.currentColors

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val rect = RectF()
    private val pad = 3f.dpToPx(context)
    private val cornerR = 8f.dpToPx(context)

    private val COLS = 5
    private val ROWS = 4

    // Grid data: [row][col] = Pair(label, score) where score determines color
    private val grid = arrayOf(
        arrayOf(P("mic", -1), P("40", 40), P("60", 60), P("40", 40), P("del", -1)),
        arrayOf(P("<", -1), P("80", 80), P("100", 100), P("80", 80), P(">", -1)),
        arrayOf(P("lang", -1), P("40", 40), P("60", 60), P("40", 40), P("space", -1)),
        arrayOf(P("shift", -1), P("20", 20), P("20", 20), P("20", 20), P("enter", -1))
    )

    private fun P(label: String, score: Int) = Pair(label, score)

    private fun colorForScore(score: Int): Int = when (score) {
        100 -> Color.parseColor("#ef4444")  // red - highest
        80 -> Color.parseColor("#f97316")   // orange
        60 -> Color.parseColor("#eab308")   // yellow
        40 -> Color.parseColor("#22c55e")   // green
        20 -> Color.parseColor("#94a3b8")   // gray
        else -> colors.utilKeyBg            // utility keys
    }

    private fun labelForScore(score: Int): String = when (score) {
        100 -> "HIGH"
        80 -> "mid"
        60 -> "mid"
        40 -> "low"
        20 -> "s.low"
        else -> ""
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val colW = w / COLS
        val rowH = h / ROWS

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val (label, score) = grid[row][col]
                val x = col * colW + pad
                val y = row * rowH + pad
                rect.set(x, y, x + colW - pad * 2, y + rowH - pad * 2)

                // Background color
                bgPaint.color = colorForScore(score)
                if (score == -1) bgPaint.alpha = 120 else bgPaint.alpha = 200
                canvas.drawRoundRect(rect, cornerR, cornerR, bgPaint)

                val cx = rect.centerX()
                val cy = rect.centerY()

                if (score > 0) {
                    // Score number
                    scorePaint.textSize = 14f.spToPx(context)
                    scorePaint.color = Color.WHITE
                    scorePaint.isFakeBoldText = score == 100
                    canvas.drawText(score.toString(), cx, cy - 2f.dpToPx(context), scorePaint)
                    scorePaint.isFakeBoldText = false

                    // Label below score
                    scorePaint.textSize = 9f.spToPx(context)
                    scorePaint.color = Color.argb(200, 255, 255, 255)
                    canvas.drawText(labelForScore(score), cx, cy + 10f.dpToPx(context), scorePaint)
                } else {
                    // Utility key label
                    textPaint.textSize = 10f.spToPx(context)
                    textPaint.color = colors.textColor
                    textPaint.alpha = 150
                    canvas.drawText(label, cx, cy + 4f.dpToPx(context), textPaint)
                }
            }
        }
    }
}
