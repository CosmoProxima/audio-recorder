package com.audio.audiorecorder

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.audio.audiorecorder.adapters.Adapters
import com.audio.audiorecorder.databases.ProjectDatabase
import com.audio.audiorecorder.databinding.ActivityShowRecordsBinding
import com.audio.audiorecorder.interfaces.OnItemClickListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShowRecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowRecordsBinding
    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var recordAdapter: Adapters
    private lateinit var db: ProjectDatabase
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        records = ArrayList()
        mediaPlayer = MediaPlayer()

        db = Room.databaseBuilder(
            this,
            ProjectDatabase::class.java,
            "audioRecords"
        ).build()

        recordAdapter = Adapters()


        binding.recyclerView.apply {
            adapter = recordAdapter
            layoutManager = LinearLayoutManager(context)
        }

        recordAdapter.setClickListener(object : OnItemClickListener {
            override fun onItemClickListener(position: Int, audioRecord: AudioRecord) {
                if(!mediaPlayer.isPlaying){
                    mediaPlayer.reset()
                    mediaPlayer.apply {
                        setDataSource(audioRecord.filePath)
                        prepare()
                        start()
                    }
                    Log.d("Position State", "$position 1")
                    recordAdapter.updatePosition(position, 1)
                }else{
                    mediaPlayer.pause()
                    recordAdapter.updatePosition(position, 0)

                }

            }
        })
        fetchAll()
    }

    private fun fetchAll() {
        GlobalScope.launch {
            records.clear()
            val queryResult = db.insertAudioRecord().getAllAudioRecords()
            Log.d("Records", "fetchAll: $queryResult")
            records.addAll(queryResult)
            recordAdapter.updateList(records)

            recordAdapter.notifyDataSetChanged()
        }
    }
}