package com.Jnx03.thaiflickkeyboard.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx
import com.Jnx03.thaiflickkeyboard.util.spToPx

class TutorialAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class AnimationType {
        TAP, FLICK_ALL, FLICK_LEFT, FLICK_UP, FLICK_RIGHT, FLICK_DOWN
    }

    var animationType: AnimationType = AnimationType.TAP
        set(value) { field = value; setupAnimation() }

    var keyLabel: String = "ก"
        set(value) { field = value; invalidate() }

    var flickLabels: Map<String, String> = emptyMap()
        set(value) { field = value; invalidate() }

    private inline val colors get() = ThemeManager.currentColors

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val fingerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fingerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val balloonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val balloonRect = RectF()
    private val cornerR = 12f.dpToPx(context)

    // Animation state
    private var animProgress = 0f // 0..1
    private var animator: ValueAnimator? = null

    // Finger position (relative to key center)
    private var fingerX = 0f
    private var fingerY = 0f
    private var fingerAlpha = 0
    private var showBalloon = false
    private var activeDirection = ""

    // Flick sequence state for FLICK_ALL
    private var currentFlickIndex = 0
    private val flickSequence = listOf("up", "right", "down", "left")

    init {
        fingerShadowPaint.color = Color.argb(40, 0, 0, 0)
        setupAnimation()
    }

    private fun setupAnimation() {
        animator?.cancel()

        val duration = when (animationType) {
            AnimationType.TAP -> 2000L
            AnimationType.FLICK_ALL -> 8000L
            else -> 2500L
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                animProgress = anim.animatedValue as Float
                updateFingerState()
                invalidate()
            }
            start()
        }
    }

    private fun updateFingerState() {
        val keySize = minOf(width, height) * 0.35f
        val flickDist = keySize * 0.8f

        when (animationType) {
            AnimationType.TAP -> {
                // 0-0.2: approach, 0.2-0.4: touch, 0.4-0.6: press, 0.6-0.8: release, 0.8-1: rest
                fingerX = 0f
                fingerY = when {
                    animProgress < 0.2f -> -keySize * (1f - animProgress / 0.2f)
                    animProgress < 0.6f -> 0f
                    animProgress < 0.8f -> -keySize * ((animProgress - 0.6f) / 0.2f)
                    else -> -keySize
                }
                fingerAlpha = when {
                    animProgress < 0.1f -> (animProgress / 0.1f * 200).toInt()
                    animProgress < 0.7f -> 200
                    animProgress < 0.85f -> (200 * (1f - (animProgress - 0.7f) / 0.15f)).toInt()
                    else -> 0
                }
                showBalloon = false
                activeDirection = ""
            }
            AnimationType.FLICK_ALL -> {
                // Cycle through 4 directions
                val phase = animProgress * 4f
                currentFlickIndex = phase.toInt().coerceIn(0, 3)
                val subProgress = phase - currentFlickIndex

                val dir = flickSequence[currentFlickIndex]
                updateSingleFlick(dir, subProgress, flickDist)
            }
            else -> {
                val dir = when (animationType) {
                    AnimationType.FLICK_LEFT -> "left"
                    AnimationType.FLICK_UP -> "up"
                    AnimationType.FLICK_RIGHT -> "right"
                    AnimationType.FLICK_DOWN -> "down"
                    else -> "up"
                }
                updateSingleFlick(dir, animProgress, flickDist)
            }
        }
    }

    private fun updateSingleFlick(dir: String, progress: Float, flickDist: Float) {
        // 0-0.15: approach, 0.15-0.35: touch, 0.35-0.6: flick, 0.6-0.8: hold, 0.8-1: release
        val flickAmount = when {
            progress < 0.35f -> 0f
            progress < 0.6f -> (progress - 0.35f) / 0.25f
            progress < 0.8f -> 1f
            else -> 1f - (progress - 0.8f) / 0.2f
        }

        val dx = when (dir) { "left" -> -flickDist; "right" -> flickDist; else -> 0f }
        val dy = when (dir) { "up" -> -flickDist; "down" -> flickDist; else -> 0f }

        fingerX = dx * flickAmount
        fingerY = when {
            progress < 0.15f -> -(minOf(width, height) * 0.35f) * (1f - progress / 0.15f)
            else -> dy * flickAmount
        }

        fingerAlpha = when {
            progress < 0.08f -> (progress / 0.08f * 200).toInt()
            progress < 0.85f -> 200
            progress < 1f -> (200 * (1f - (progress - 0.85f) / 0.15f)).toInt()
            else -> 0
        }

        showBalloon = flickAmount > 0.3f
        activeDirection = if (showBalloon) dir else ""
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val keySize = minOf(width, height) * 0.35f
        val halfKey = keySize / 2f

        // Draw key background
        rect.set(cx - halfKey, cy - halfKey, cx + halfKey, cy + halfKey)
        keyBgPaint.color = if (fingerAlpha > 100 && !showBalloon) colors.charKeyPressed else colors.charKeyBg
        canvas.drawRoundRect(rect, cornerR, cornerR, keyBgPaint)

        // Draw main character
        textPaint.textSize = 28f.spToPx(context)
        textPaint.color = colors.textColor
        textPaint.isFakeBoldText = false
        canvas.drawText(toDisplayChar(keyLabel), cx, cy + 10f.dpToPx(context), textPaint)

        // Draw flick hints
        hintPaint.textSize = 11f.spToPx(context)
        hintPaint.color = colors.hintColor
        flickLabels["up"]?.let { canvas.drawText(toDisplayChar(it), cx, cy - halfKey + 16f.dpToPx(context), hintPaint) }
        flickLabels["down"]?.let { canvas.drawText(toDisplayChar(it), cx, cy + halfKey - 6f.dpToPx(context), hintPaint) }
        flickLabels["left"]?.let { canvas.drawText(toDisplayChar(it), cx - halfKey + 16f.dpToPx(context), cy + 4f.dpToPx(context), hintPaint) }
        flickLabels["right"]?.let { canvas.drawText(toDisplayChar(it), cx + halfKey - 16f.dpToPx(context), cy + 4f.dpToPx(context), hintPaint) }

        // Draw flick balloons when active
        if (showBalloon) {
            val balloonSize = keySize * 0.85f
            val gap = 6f.dpToPx(context)
            drawDirectionBalloon(canvas, cx, cy, halfKey, balloonSize, gap, "up")
            drawDirectionBalloon(canvas, cx, cy, halfKey, balloonSize, gap, "down")
            drawDirectionBalloon(canvas, cx, cy, halfKey, balloonSize, gap, "left")
            drawDirectionBalloon(canvas, cx, cy, halfKey, balloonSize, gap, "right")
        }

        // Draw finger
        if (fingerAlpha > 0) {
            val fX = cx + fingerX
            val fY = cy + fingerY
            val fingerR = 18f.dpToPx(context)

            // Shadow
            fingerShadowPaint.color = Color.argb(fingerAlpha / 5, 0, 0, 0)
            canvas.drawCircle(fX + 2f, fY + 3f, fingerR + 2f, fingerShadowPaint)

            // Finger circle
            fingerPaint.color = Color.argb(fingerAlpha, 100, 149, 237) // Cornflower blue
            canvas.drawCircle(fX, fY, fingerR, fingerPaint)

            // Inner highlight
            fingerPaint.color = Color.argb(fingerAlpha / 2, 255, 255, 255)
            canvas.drawCircle(fX - 4f, fY - 4f, fingerR * 0.4f, fingerPaint)
        }
    }

    private fun drawDirectionBalloon(
        canvas: Canvas, cx: Float, cy: Float, halfKey: Float,
        balloonSize: Float, gap: Float, dir: String
    ) {
        val label = flickLabels[dir] ?: return
        val halfBalloon = balloonSize / 2f

        val bx: Float
        val by: Float
        when (dir) {
            "up" -> { bx = cx; by = cy - halfKey - gap - halfBalloon }
            "down" -> { bx = cx; by = cy + halfKey + gap + halfBalloon }
            "left" -> { bx = cx - halfKey - gap - halfBalloon; by = cy }
            "right" -> { bx = cx + halfKey + gap + halfBalloon; by = cy }
            else -> return
        }

        balloonRect.set(bx - halfBalloon, by - halfBalloon, bx + halfBalloon, by + halfBalloon)
        val isActive = dir == activeDirection
        balloonPaint.color = if (isActive) colors.charKeyPressed else colors.flickBalloonBg
        canvas.drawRoundRect(balloonRect, cornerR, cornerR, balloonPaint)

        textPaint.textSize = 22f.spToPx(context)
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = isActive
        canvas.drawText(toDisplayChar(label), bx, by + 8f.dpToPx(context), textPaint)
        textPaint.isFakeBoldText = false
    }

    private fun toDisplayChar(char: String): String {
        return FlickKeyboardView.toDisplayChar(char)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupAnimation()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
