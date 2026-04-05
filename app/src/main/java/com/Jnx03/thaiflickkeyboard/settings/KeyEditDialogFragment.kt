package com.Jnx03.thaiflickkeyboard.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.model.FlickKey
import com.google.android.material.textfield.TextInputEditText

class KeyEditDialogFragment : DialogFragment() {

    var onKeyEdited: ((Int, FlickKey) -> Unit)? = null

    private val colorOptions = listOf(
        "#22c55e", "#6366f1", "#f59e0b", "#ec4899", "#a855f7", "#64748b", "#ef4444"
    )
    private var selectedColor = "#22c55e"

    companion object {
        private const val ARG_POSITION = "position"
        private const val ARG_ID = "id"
        private const val ARG_TAP = "tap"
        private const val ARG_LEFT = "left"
        private const val ARG_UP = "up"
        private const val ARG_RIGHT = "right"
        private const val ARG_DOWN = "down"
        private const val ARG_COLOR = "color"
        private const val ARG_HINT = "hint"

        fun newInstance(position: Int, key: FlickKey): KeyEditDialogFragment {
            return KeyEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                    putString(ARG_ID, key.id)
                    putString(ARG_TAP, key.tap)
                    putString(ARG_LEFT, key.left)
                    putString(ARG_UP, key.up)
                    putString(ARG_RIGHT, key.right)
                    putString(ARG_DOWN, key.down)
                    putString(ARG_COLOR, key.color)
                    putString(ARG_HINT, key.hint)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val position = args.getInt(ARG_POSITION)
        selectedColor = args.getString(ARG_COLOR, "#22c55e")

        val view = layoutInflater.inflate(R.layout.dialog_key_edit, null)

        val etTap = view.findViewById<TextInputEditText>(R.id.et_tap)
        val etLeft = view.findViewById<TextInputEditText>(R.id.et_left)
        val etUp = view.findViewById<TextInputEditText>(R.id.et_up)
        val etRight = view.findViewById<TextInputEditText>(R.id.et_right)
        val etDown = view.findViewById<TextInputEditText>(R.id.et_down)
        val etHint = view.findViewById<TextInputEditText>(R.id.et_hint)

        etTap.setText(args.getString(ARG_TAP))
        etLeft.setText(args.getString(ARG_LEFT))
        etUp.setText(args.getString(ARG_UP))
        etRight.setText(args.getString(ARG_RIGHT))
        etDown.setText(args.getString(ARG_DOWN))
        etHint.setText(args.getString(ARG_HINT))

        // Color picker
        val colorRow = view.findViewById<LinearLayout>(R.id.color_picker_row)
        setupColorPicker(colorRow)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_key_title)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                val editedKey = FlickKey(
                    id = args.getString(ARG_ID, ""),
                    tap = etTap.text?.toString() ?: "",
                    left = etLeft.text?.toString() ?: "",
                    up = etUp.text?.toString() ?: "",
                    right = etRight.text?.toString() ?: "",
                    down = etDown.text?.toString() ?: "",
                    color = selectedColor,
                    hint = etHint.text?.toString() ?: ""
                )
                onKeyEdited?.invoke(position, editedKey)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun setupColorPicker(container: LinearLayout) {
        container.removeAllViews()
        val size = (40 * resources.displayMetrics.density).toInt()
        val margin = (6 * resources.displayMetrics.density).toInt()

        for (colorHex in colorOptions) {
            val swatch = View(requireContext()).apply {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(colorHex))
                    if (colorHex == selectedColor) {
                        setStroke((3 * resources.displayMetrics.density).toInt(), Color.WHITE)
                    }
                }
                background = bg
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                setOnClickListener {
                    selectedColor = colorHex
                    setupColorPicker(container)
                }
            }
            container.addView(swatch)
        }
    }
}
