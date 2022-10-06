package com.audio.audiorecorder

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName="audioRecords")
data class AudioRecord(
    var filename:String,
    var filePath:String,
    var timestamp:Long,
    var duration:String,
    var empsPath:String,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @Ignore
    var isChecked = false
}