package com.example.clockapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 自定义时钟 View：在 onDraw 中绘制表盘与三根指针，每秒刷新实现跳动。
 */
class AnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.BLACK
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.BLACK
    }

    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
    }

    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.DKGRAY
        strokeCap = Paint.Cap.ROUND
    }

    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.RED
        strokeCap = Paint.Cap.ROUND
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 1000L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(tickRunnable)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 2f * 0.9f

        canvas.drawCircle(centerX, centerY, radius, dialPaint)

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val inner = radius * 0.85f
            val outer = radius * 0.95f
            canvas.drawLine(
                centerX + inner * cos(angle).toFloat(),
                centerY + inner * sin(angle).toFloat(),
                centerX + outer * cos(angle).toFloat(),
                centerY + outer * sin(angle).toFloat(),
                tickPaint,
            )
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) % 12
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        drawHand(canvas, centerX, centerY, radius * 0.5f, (hour + minute / 60f) * 30f, hourPaint)
        drawHand(canvas, centerX, centerY, radius * 0.75f, (minute + second / 60f) * 6f, minutePaint)
        drawHand(canvas, centerX, centerY, radius * 0.9f, second * 6f, secondPaint)

        canvas.drawCircle(centerX, centerY, 12f, centerPaint)
    }

    private fun drawHand(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        length: Float,
        degrees: Float,
        paint: Paint,
    ) {
        val rad = Math.toRadians(degrees.toDouble())
        val endX = centerX + length * sin(rad).toFloat()
        val endY = centerY - length * cos(rad).toFloat()
        canvas.drawLine(centerX, centerY, endX, endY, paint)
    }
}
