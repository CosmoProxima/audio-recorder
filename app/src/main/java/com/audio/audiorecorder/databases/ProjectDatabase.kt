package com.audio.audiorecorder.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.audio.audiorecorder.AudioRecord
import com.audio.audiorecorder.interfaces.AudioRecordsInterface

@Database(entities = arrayOf(AudioRecord::class), version = 1)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun insertAudioRecord() : AudioRecordsInterface
}