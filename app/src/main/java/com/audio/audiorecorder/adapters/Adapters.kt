package com.audio.audiorecorder.adapters

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.audio.audiorecorder.AudioRecord
import com.audio.audiorecorder.R
import com.audio.audiorecorder.databinding.RecordlistLayoutBinding
import com.audio.audiorecorder.interfaces.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.*

class Adapters :
    RecyclerView.Adapter<Adapters.ViewHolder>() {

    private val list = mutableListOf<AudioRecord>()
    private lateinit var onItemClickListener: OnItemClickListener
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    var playingPosition: Int = -1
    var state = 0

    class ViewHolder(private val binding: RecordlistLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bindRecordData(
            data: AudioRecord,
            position: Int,
            onItemClickListener: OnItemClickListener,
            playingPosition: Int,
            state: Int
        ) {

            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = Date(data.timestamp)
            val strDate = simpleDateFormat.format(date)

            binding.tvFileName.text = data.filename
            binding.tvMetaData.text = "${data?.duration} $strDate"

            Log.d("bindRecordData", "Position $playingPosition State $state")
            Log.d("AdapterPosition", "$position")

            binding.playPauseRecord.setOnClickListener {
                onItemClickListener.onItemClickListener(position, data)
                if (position == playingPosition && state == 1) {
                    binding.playPauseRecord.setImageResource(R.drawable.ic_baseline_play)
                } else if (position == playingPosition && state == 0) {
                    binding.playPauseRecord.setImageResource(R.drawable.ic_baseline_pause_black)
                }
            }
        }

    }

    fun updatePosition(position: Int, state: Int) {
        this.playingPosition = position
        this.state = state
        notifyDataSetChanged()
        Log.d("UpdatePosition", "Position State: $position $state")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapters.ViewHolder {
        return ViewHolder(

            RecordlistLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Adapters.ViewHolder, position: Int) {
        holder.bindRecordData(list[position], position, onItemClickListener, playingPosition, state)
    }

    override fun getItemCount(): Int {
        Log.d("List Size", "getItemCount: ${list.size}")
        return list.size
    }

    fun setClickListener(recordClickListener: OnItemClickListener) {
        this.onItemClickListener = recordClickListener
    }

    fun updateList(list: List<AudioRecord>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
}