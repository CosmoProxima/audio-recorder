package com.audio.audiorecorder.interfaces

import androidx.room.*
import com.audio.audiorecorder.AudioRecord

@Dao
interface AudioRecordsInterface {
    @Query("SELECT * FROM audioRecords")
    fun getAllAudioRecords():List<AudioRecord>

    @Insert
    fun insert(vararg audioRecord:AudioRecord)

    @Delete
    fun deleteRecord(audioRecord: AudioRecord)

    @Delete
    fun deleteMultipleRecord(audioRecords: Array<AudioRecord>)

    @Update
    fun updateAudioRecord(audioRecord: AudioRecord)
}