package com.Jnx03.thaiflickkeyboard.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.data.LayoutRepository
import com.Jnx03.thaiflickkeyboard.model.FlickKey
import com.Jnx03.thaiflickkeyboard.model.KeyboardLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class LayoutCustomizationActivity : AppCompatActivity() {

    private lateinit var layoutRepository: LayoutRepository
    private lateinit var adapter: KeyAdapter
    private var currentLayout: KeyboardLayout = KeyboardLayout.padPimOpti()
    private val editableKeys = mutableListOf<FlickKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_customization)

        layoutRepository = LayoutRepository(this)
        currentLayout = layoutRepository.loadLayout()
        editableKeys.clear()
        editableKeys.addAll(currentLayout.keys)

        val rv = findViewById<RecyclerView>(R.id.rv_keys)
        adapter = KeyAdapter(editableKeys) { position ->
            val key = editableKeys[position]
            if (key.id != "backspace" && key.id != "mode") {
                showKeyEditDialog(position, key)
            }
        }
        rv.layoutManager = GridLayoutManager(this, 4)
        rv.adapter = adapter

        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {
            val newLayout = currentLayout.copy(keys = editableKeys.toList())
            layoutRepository.saveLayout(newLayout)
            Toast.makeText(this, "Layout saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btn_reset).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    layoutRepository.resetToDefault()
                    editableKeys.clear()
                    editableKeys.addAll(KeyboardLayout.padPimOpti().keys)
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showKeyEditDialog(position: Int, key: FlickKey) {
        val fragment = KeyEditDialogFragment.newInstance(position, key)
        fragment.onKeyEdited = { pos, editedKey ->
            editableKeys[pos] = editedKey
            adapter.notifyItemChanged(pos)
        }
        fragment.show(supportFragmentManager, "key_edit")
    }

    private class KeyAdapter(
        private val keys: List<FlickKey>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<KeyAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view as MaterialCardView
            val tvTap: TextView = view.findViewById(R.id.tv_tap)
            val tvLeft: TextView = view.findViewById(R.id.tv_left)
            val tvRight: TextView = view.findViewById(R.id.tv_right)
            val tvUp: TextView = view.findViewById(R.id.tv_up)
            val tvDown: TextView = view.findViewById(R.id.tv_down)
            val tvHint: TextView = view.findViewById(R.id.tv_hint)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_key_edit, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val key = keys[position]
            holder.tvTap.text = key.tap
            holder.tvLeft.text = key.left
            holder.tvRight.text = key.right
            holder.tvUp.text = key.up
            holder.tvDown.text = key.down
            holder.tvHint.text = key.hint

            try {
                val color = Color.parseColor(key.color)
                holder.card.strokeColor = color
                holder.card.strokeWidth = 2
            } catch (_: Exception) {
            }

            holder.card.setOnClickListener { onClick(position) }
        }

        override fun getItemCount() = keys.size
    }
}
