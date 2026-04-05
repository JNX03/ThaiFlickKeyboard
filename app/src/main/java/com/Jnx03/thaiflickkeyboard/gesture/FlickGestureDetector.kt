package com.Jnx03.thaiflickkeyboard.gesture

import com.Jnx03.thaiflickkeyboard.model.FlickDirection
import kotlin.math.atan2
import kotlin.math.sqrt

class FlickGestureDetector(private val deadZoneRadiusPx: Float) {

    private var originX = 0f
    private var originY = 0f
    private var currentDirection = FlickDirection.TAP

    fun onTouchDown(x: Float, y: Float) {
        originX = x
        originY = y
        currentDirection = FlickDirection.TAP
    }

    fun onTouchMove(x: Float, y: Float): FlickDirection {
        val dx = x - originX
        val dy = y - originY
        val distance = sqrt(dx * dx + dy * dy)

        currentDirection = if (distance < deadZoneRadiusPx) {
            FlickDirection.TAP
        } else {
            directionFromAngle(dx, dy)
        }
        return currentDirection
    }

    fun onTouchUp(): FlickDirection {
        return currentDirection
    }

    fun reset() {
        currentDirection = FlickDirection.TAP
    }

    private fun directionFromAngle(dx: Float, dy: Float): FlickDirection {
        // atan2 with negated dy because screen Y is inverted
        val angle = Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble()))
        return when {
            angle in -45.0..45.0 -> FlickDirection.RIGHT
            angle > 45.0 && angle <= 135.0 -> FlickDirection.UP
            angle < -45.0 && angle >= -135.0 -> FlickDirection.DOWN
            else -> FlickDirection.LEFT
        }
    }
}
