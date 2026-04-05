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
import com.Jnx03.thaiflickkeyboard.gesture.FlickGestureDetector
import com.Jnx03.thaiflickkeyboard.model.FlickDirection
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class UtilityBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSpace: (() -> Unit)? = null
    var onEnter: (() -> Unit)? = null
    var onLanguageSwitch: (() -> Unit)? = null
    var onCursorLeft: (() -> Unit)? = null
    var onCursorRight: (() -> Unit)? = null

    private val gestureDetector = FlickGestureDetector(15f.dpToPx(context))
    private var activeRegion = -1 // 0=lang, 1=space, 2=enter
    private var isTouching = false

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2a2a4a")
    }
    private val activeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3a3a5a")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 14f.spToPx(context)
    }

    private val padding = 4f.dpToPx(context)
    private val cornerRadius = 6f.dpToPx(context)
    private val rect = RectF()

    private val langIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_language_switch)
    private val enterIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_enter)

    // Region boundaries (calculated in onSizeChanged)
    private var langRight = 0f
    private var enterLeft = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        langRight = w * 0.15f
        enterLeft = w * 0.85f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val h = height.toFloat()

        // Language switch button
        rect.set(padding, padding, langRight - padding, h - padding)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius,
            if (isTouching && activeRegion == 0) activeBgPaint else bgPaint)
        langIcon?.let {
            val iconSize = (h * 0.45f).toInt()
            val cx = rect.centerX().toInt()
            val cy = rect.centerY().toInt()
            it.setBounds(cx - iconSize / 2, cy - iconSize / 2, cx + iconSize / 2, cy + iconSize / 2)
            it.draw(canvas)
        }

        // Space bar
        rect.set(langRight + padding, padding, enterLeft - padding, h - padding)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius,
            if (isTouching && activeRegion == 1) activeBgPaint else bgPaint)
        canvas.drawText("Space", rect.centerX(), rect.centerY() + textPaint.textSize / 3, textPaint)

        // Enter button
        rect.set(enterLeft + padding, padding, width - padding, h - padding)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius,
            if (isTouching && activeRegion == 2) activeBgPaint else bgPaint)
        enterIcon?.let {
            val iconSize = (h * 0.45f).toInt()
            val cx = rect.centerX().toInt()
            val cy = rect.centerY().toInt()
            it.setBounds(cx - iconSize / 2, cy - iconSize / 2, cx + iconSize / 2, cy + iconSize / 2)
            it.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeRegion = getRegion(event.x)
                isTouching = true
                if (activeRegion == 1) {
                    gestureDetector.onTouchDown(event.x, event.y)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (activeRegion == 1) {
                    gestureDetector.onTouchMove(event.x, event.y)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                when (activeRegion) {
                    0 -> onLanguageSwitch?.invoke()
                    1 -> {
                        val dir = gestureDetector.onTouchUp()
                        when (dir) {
                            FlickDirection.LEFT -> onCursorLeft?.invoke()
                            FlickDirection.RIGHT -> onCursorRight?.invoke()
                            else -> onSpace?.invoke()
                        }
                    }
                    2 -> onEnter?.invoke()
                }
                activeRegion = -1
                isTouching = false
                gestureDetector.reset()
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                activeRegion = -1
                isTouching = false
                gestureDetector.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getRegion(x: Float): Int {
        return when {
            x < langRight -> 0
            x > enterLeft -> 2
            else -> 1
        }
    }
}
