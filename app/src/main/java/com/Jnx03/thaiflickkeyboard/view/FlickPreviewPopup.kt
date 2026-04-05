package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.util.dpToPx

class FlickPreviewPopup(private val context: Context) {

    private var popupWindow: PopupWindow? = null
    private var previewText: TextView? = null

    private fun ensurePopup() {
        if (popupWindow == null) {
            val view = LayoutInflater.from(context).inflate(R.layout.preview_popup, null)
            previewText = view.findViewById(R.id.preview_text)
            popupWindow = PopupWindow(
                view,
                (56f.dpToPx(context)).toInt(),
                (56f.dpToPx(context)).toInt()
            ).apply {
                isClippingEnabled = false
                isTouchable = false
            }
        }
    }

    fun show(anchor: View, text: String, color: Int, x: Int, y: Int) {
        ensurePopup()
        previewText?.text = text

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f.dpToPx(context)
            setColor(Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)))
        }
        popupWindow?.contentView?.background = bg

        try {
            if (popupWindow?.isShowing == true) {
                popupWindow?.update(
                    x - (28f.dpToPx(context)).toInt(),
                    y,
                    -1, -1
                )
            } else {
                popupWindow?.showAtLocation(anchor, Gravity.NO_GRAVITY,
                    x - (28f.dpToPx(context)).toInt(),
                    y
                )
            }
        } catch (_: Exception) {
            // Popup may fail if view is not attached
        }
    }

    fun dismiss() {
        try {
            popupWindow?.dismiss()
        } catch (_: Exception) {
        }
    }
}
