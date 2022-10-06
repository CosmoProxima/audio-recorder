package com.audio.audiorecorder.interfaces

import com.audio.audiorecorder.AudioRecord

interface OnItemClickListener {
    fun onItemClickListener(position:Int, data: AudioRecord)
}