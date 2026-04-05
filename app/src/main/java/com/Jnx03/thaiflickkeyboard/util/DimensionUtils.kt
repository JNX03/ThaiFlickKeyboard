package com.Jnx03.thaiflickkeyboard.util

import android.content.Context
import android.util.TypedValue

fun Float.dpToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics
    )
}

fun Int.dpToPx(context: Context): Float {
    return this.toFloat().dpToPx(context)
}

fun Float.spToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics
    )
}
