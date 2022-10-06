package com.audio.audiorecorder.extras

import android.os.Handler
import android.os.Looper

class Timer(timeListener:OnTimerTickListener) {

    interface OnTimerTickListener{
        fun onTimerTick(duration:String)
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration = 0L
    private var delay = 100L

    fun start(){
        handler.postDelayed(runnable, delay)
    }

    fun pause(){
        handler.removeCallbacks(runnable)

    }

    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    fun timer_Format(): String {
        val milliseconds: Long = duration % 1000
        val seconds: Long = (duration / 1000) % 60
        val minutes: Long = (duration / (1000 * 60)) % 60
        val hours: Long = (duration / (1000 * 60 * 60))

        return if (hours > 0)
            "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, milliseconds/10)
        else
            "%02d:%02d.%02d".format(minutes, seconds, milliseconds/10)
    }

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            timeListener.onTimerTick(timer_Format())
        }
    }
}


