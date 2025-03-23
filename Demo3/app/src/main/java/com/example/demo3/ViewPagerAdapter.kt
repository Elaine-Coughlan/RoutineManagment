package com.example.demo3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RoutineFragment()
            1 -> ProgressFragment()
            2 -> AchievementsFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}
