package com.Jnx03.thaiflickkeyboard.view

import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.Jnx03.thaiflickkeyboard.util.ThemeManager

class EmojiGridAdapter(
    private var emojis: List<String>,
    private val onEmojiClick: (String) -> Unit
) : RecyclerView.Adapter<EmojiGridAdapter.EmojiViewHolder>() {

    class EmojiViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val cellSize = parent.width / 8
        val tv = TextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(cellSize, (cellSize * 1.1f).toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            gravity = Gravity.CENTER
            // Ripple effect
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }
        return EmojiViewHolder(tv)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.textView.text = emojis[position]
        holder.textView.setOnClickListener { onEmojiClick(emojis[position]) }
    }

    override fun getItemCount(): Int = emojis.size

    fun updateEmojis(newEmojis: List<String>) {
        emojis = newEmojis
        notifyDataSetChanged()
    }
}
