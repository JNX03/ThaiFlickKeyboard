package com.Jnx03.thaiflickkeyboard.model

enum class FlickDirection {
    TAP, LEFT, UP, RIGHT, DOWN
}

data class FlickKey(
    val id: String,
    val tap: String,
    val left: String,
    val up: String,
    val right: String,
    val down: String,
    val color: String,
    val hint: String
) {
    fun charForDirection(direction: FlickDirection): String {
        return when (direction) {
            FlickDirection.TAP -> tap
            FlickDirection.LEFT -> left
            FlickDirection.UP -> up
            FlickDirection.RIGHT -> right
            FlickDirection.DOWN -> down
        }
    }
}
