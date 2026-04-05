package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.Jnx03.thaiflickkeyboard.model.FlickDirection
import com.Jnx03.thaiflickkeyboard.model.FlickKey
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class FlickCrossPopup(private val context: Context) {

    private var popupWindow: PopupWindow? = null
    private var crossView: CrossView? = null

    private val cellSize = 52f.dpToPx(context)
    private val popupWidth = (cellSize * 3).toInt()
    private val popupHeight = (cellSize * 3).toInt()

    fun show(anchor: View, key: FlickKey, direction: FlickDirection, anchorX: Int, anchorY: Int) {
        if (crossView == null) {
            crossView = CrossView(context, cellSize)
            popupWindow = PopupWindow(crossView, popupWidth, popupHeight).apply {
                isClippingEnabled = true
                isTouchable = false
                animationStyle = 0
            }
        }

        crossView?.setKey(key, direction)

        val x = anchorX - popupWidth / 2
        val y = anchorY - popupHeight - (16f.dpToPx(context)).toInt()

        try {
            if (popupWindow?.isShowing == true) {
                popupWindow?.update(x, y, -1, -1)
            } else {
                popupWindow?.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
            }
        } catch (_: Exception) {}
    }

    fun updateDirection(direction: FlickDirection) {
        crossView?.updateDirection(direction)
    }

    fun dismiss() {
        try { popupWindow?.dismiss() } catch (_: Exception) {}
    }

    private class CrossView(
        context: Context,
        private val cellSize: Float
    ) : View(context) {

        private var key: FlickKey? = null
        private var activeDirection = FlickDirection.TAP

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#404040")
        }
        private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4285f4")
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#e8e8e8")
            textAlign = Paint.Align.CENTER
            textSize = 22f.spToPx(context)
        }
        private val highlightTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 24f.spToPx(context)
            isFakeBoldText = true
        }
        private val cornerRadius = 12f.dpToPx(context)
        private val cellPad = 2f.dpToPx(context)
        private val rect = RectF()

        fun setKey(key: FlickKey, direction: FlickDirection) {
            this.key = key
            this.activeDirection = direction
            invalidate()
        }

        fun updateDirection(direction: FlickDirection) {
            if (this.activeDirection != direction) {
                this.activeDirection = direction
                invalidate()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val k = key ?: return

            // 3x3 grid, only cross cells (center, top, left, right, bottom)
            // Layout:   [  ] [UP] [  ]
            //           [LT] [CT] [RT]
            //           [  ] [DN] [  ]

            drawCell(canvas, 1, 0, k.up, FlickDirection.UP)
            drawCell(canvas, 0, 1, k.left, FlickDirection.LEFT)
            drawCell(canvas, 1, 1, k.tap, FlickDirection.TAP)
            drawCell(canvas, 2, 1, k.right, FlickDirection.RIGHT)
            drawCell(canvas, 1, 2, k.down, FlickDirection.DOWN)
        }

        private fun drawCell(canvas: Canvas, col: Int, row: Int, char: String, dir: FlickDirection) {
            if (char.isEmpty()) return

            val left = col * cellSize + cellPad
            val top = row * cellSize + cellPad
            val right = (col + 1) * cellSize - cellPad
            val bottom = (row + 1) * cellSize - cellPad
            rect.set(left, top, right, bottom)

            val isActive = dir == activeDirection
            val paint = if (isActive) highlightPaint else bgPaint
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            val displayChar = toDisplayChar(char)
            val tp = if (isActive) highlightTextPaint else textPaint
            val textY = rect.centerY() + tp.textSize / 3
            canvas.drawText(displayChar, rect.centerX(), textY, tp)
        }

        private fun toDisplayChar(char: String): String {
            if (char.isEmpty()) return char
            val cp = char.codePointAt(0)
            // Thai combining characters need a base consonant for display
            val isCombining = cp == 0x0E31 || // mai han akat (ั)
                    (cp in 0x0E34..0x0E3A) || // sara i, ii, ue, uee, u, uu, phinthu
                    (cp in 0x0E47..0x0E4E)    // maitaikhu, mai ek, mai tho, mai tri, mai chattawa, thanthakhat, nikhahit, yamakkan
            return if (isCombining) "ก$char" else char
        }
    }
}
