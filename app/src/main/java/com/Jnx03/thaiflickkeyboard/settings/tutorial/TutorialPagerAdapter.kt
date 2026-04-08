package com.Jnx03.thaiflickkeyboard.settings.tutorial

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TutorialPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = STEP_COUNT

    override fun createFragment(position: Int) = TutorialStepFragment.newInstance(position)

    companion object {
        const val STEP_COUNT = 7
    }
}
