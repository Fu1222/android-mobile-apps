package com.example.clockapp.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clockapp.R
import java.util.Calendar

class DigitalClockFragment : Fragment() {

    private lateinit var clockText: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_digital_clock, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clockText = view.findViewById(R.id.digital_clock)
        updateTime()
        handler.postDelayed(updateRunnable, 1000L)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroyView()
    }

    private fun updateTime() {
        val calendar = Calendar.getInstance()
        clockText.text = String.format(
            "%02d:%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
        )
    }
}
