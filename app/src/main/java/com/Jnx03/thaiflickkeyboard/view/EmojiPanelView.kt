package com.Jnx03.thaiflickkeyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Jnx03.thaiflickkeyboard.data.EmojiData
import com.Jnx03.thaiflickkeyboard.data.RecentEmojiManager
import com.Jnx03.thaiflickkeyboard.util.ThemeManager
import com.Jnx03.thaiflickkeyboard.util.dpToPx

class EmojiPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onEmojiSelected: ((String) -> Unit)? = null

    private val recentManager = RecentEmojiManager(context)
    private val categoryBarHeight = 42f.dpToPx(context).toInt()

    private val adapter = EmojiGridAdapter(emptyList()) { emoji ->
        recentManager.addRecent(emoji)
        onEmojiSelected?.invoke(emoji)
    }

    private val recyclerView = RecyclerView(context).apply {
        layoutManager = GridLayoutManager(context, 8)
        adapter = this@EmojiPanelView.adapter
        clipToPadding = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    private val categoryBar = EmojiCategoryBar(context).apply {
        // Index 0 = recent clock icon, then 9 category icons
        categoryIcons = listOf("🕐") + EmojiData.categories.map { it.icon }
        selectedIndex = 0
        onCategorySelected = { index -> switchCategory(index) }
    }

    private val bgPaint = Paint()

    init {
        setWillNotDraw(false)

        // RecyclerView fills space above category bar
        addView(recyclerView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ).apply {
            bottomMargin = categoryBarHeight
        })

        // Category bar at bottom
        addView(categoryBar, LayoutParams(
            LayoutParams.MATCH_PARENT,
            categoryBarHeight,
            Gravity.BOTTOM
        ))

        // Show recent by default
        showRecent()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (resources.displayMetrics.heightPixels * 0.32f).toInt()
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgPaint.color = ThemeManager.currentColors.kbBg
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }

    fun showRecent() {
        val recent = recentManager.getRecent()
        if (recent.isEmpty()) {
            // If no recent, show first category
            switchCategory(1)
            categoryBar.selectedIndex = 1
        } else {
            adapter.updateEmojis(recent)
            recyclerView.scrollToPosition(0)
        }
    }

    private fun switchCategory(index: Int) {
        categoryBar.selectedIndex = index
        if (index == 0) {
            val recent = recentManager.getRecent()
            if (recent.isEmpty()) {
                // Show smileys if no recent
                adapter.updateEmojis(EmojiData.categories[0].emojis)
            } else {
                adapter.updateEmojis(recent)
            }
        } else {
            val catIndex = index - 1
            if (catIndex in EmojiData.categories.indices) {
                adapter.updateEmojis(EmojiData.categories[catIndex].emojis)
            }
        }
        recyclerView.scrollToPosition(0)
    }

    fun refreshTheme() {
        setBackgroundColor(ThemeManager.currentColors.kbBg)
        invalidate()
        categoryBar.invalidate()
    }
}
