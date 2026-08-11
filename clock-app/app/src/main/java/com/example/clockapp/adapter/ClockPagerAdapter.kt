package com.example.clockapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.clockapp.fragment.AnalogClockFragment
import com.example.clockapp.fragment.DigitalClockFragment

class ClockPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AnalogClockFragment()
        else -> DigitalClockFragment()
    }
}
