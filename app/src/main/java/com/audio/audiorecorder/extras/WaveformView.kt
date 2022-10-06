package com.audio.audiorecorder.extras

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint = Paint()
    private var amplitude = ArrayList<Float>()
    private var amplitudeLines = ArrayList<RectF>()
    private var radius = 6f
    private var w = 9f
    private var screenWidth = 0f
    private var screenHeight = 400f
    private var d = 6f
    private var maxAmplitudeLines = 0

    fun addAmplitude(amplitudeData: Float) {
        var norm = Math.min(amplitudeData.toInt() / 7, 400).toFloat()
        amplitude.add(norm)
        amplitudeLines.clear()
        var amps = amplitude.takeLast(maxAmplitudeLines)

        for (i in amps.indices) {
            val left = screenWidth - i * (w + d)
            val top = screenHeight/2 - amps[i]/2
            val right: Float = left + w
            val bottom: Float = top + amps[i]
            amplitudeLines.add(RectF(left, top, right, bottom))
        }

        invalidate()
    }

    fun clear():ArrayList<Float>{
        var amps = amplitude.clone() as ArrayList<Float>

        amplitude.clear()
        amplitudeLines.clear()
        invalidate()
        return amps
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        amplitudeLines.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }

    init {
        paint.color = Color.rgb(0, 150, 136)
        screenWidth = resources.displayMetrics.widthPixels.toFloat()
        maxAmplitudeLines = (screenWidth / (w + d)).toInt()
    }
}