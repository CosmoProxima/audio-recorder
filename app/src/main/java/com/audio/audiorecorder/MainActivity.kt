package com.audio.audiorecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.audio.audiorecorder.databases.ProjectDatabase
import com.audio.audiorecorder.databinding.ActivityMainBinding
import com.audio.audiorecorder.extras.Timer
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.audio.audiorecorder.extras.WaveformView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.collections.ArrayList

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private lateinit var amplitude: ArrayList<Float>
    private lateinit var binding: ActivityMainBinding
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<LinearLayout>
    private lateinit var db: ProjectDatabase
    private var duration = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        bottomSheetBehaviour = BottomSheetBehavior.from(binding.saveBottomSheetId.linearBottomSheet)
        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

        //Checking the permissions for Audio Recording from the user
        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        //Initialize the room database
        db = Room.databaseBuilder(
            this,
            ProjectDatabase::class.java,
            "audioRecords"
        ).build()

        //Click Listener for record the video
        binding.record.setOnClickListener {
            //Added the check for state of the recording
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecordingAudio()
            }

            //Check the build version for the Vibrations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }

            //Click Listener for Saving the recording
            binding.save.setOnClickListener {
                stopRecording()
                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bottomSheetBG.visibility = View.VISIBLE
                binding.saveBottomSheetId.fileName.setText(filename)
            }
        }

        //Click Listener for go to the Recording List page
        binding.recordList.setOnClickListener {
            Toast.makeText(this, "Recording List", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ShowRecordsActivity::class.java))
        }

        //Click Listener for deleting the recording
        binding.delete.setOnClickListener {
            stopRecording()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Recording deleted", Toast.LENGTH_LONG).show()
        }

        //Click Listener for opening the bottom sheet
        binding.bottomSheetBG.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }

        binding.saveBottomSheetId.cancelSaveRecord.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }

        binding.saveBottomSheetId.saveRecording.setOnClickListener {
            dismiss()
            save()
        }
    }

    //Function for pausing the recording
    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.pause()
            isPaused = true
            binding.record.setImageResource(R.drawable.ic_baseline_stop_circle)
            timer.pause()
        }
    }

    //Function for resuming the recording
    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.resume()
            isPaused = false
            binding.record.setImageResource(R.drawable.ic_baseline_pause)
            timer.start()
        }

        binding.delete.isClickable = false
    }

    //Override function to check the permission of the audio from user
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

        }
    }

    //Function to save the recoding in the database
    private fun save() {
        val newFileName = binding.saveBottomSheetId.fileName.text.toString()
        if (newFileName != filename) {
            val newFile = File("$dirPath$newFileName.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }

        val filePath = "$dirPath$newFileName.mp3"
        val timeStamp = Date().time
        val empsPath = "$dirPath$newFileName"

        try {
            val fos = FileOutputStream(empsPath)
            val out = ObjectOutputStream(fos)
            out.writeObject(amplitude)
            out.close()
            fos.close()
        } catch (e: IOException) {

        }

        val record = AudioRecord(newFileName, filePath, timeStamp, duration, empsPath)

        GlobalScope.launch {
            db.insertAudioRecord().insert(record)

        }
        Toast.makeText(this, "Recording is saved on your device", Toast.LENGTH_LONG).show()
    }

    //Function to close the bottom sheet
    private fun dismiss() {
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.saveBottomSheetId.fileName)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)

    }

    //Function to hide the keyboard
    private fun hideKeyboard(view: View) {
        val m = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        m.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //Function to start the audio recording
    private fun startRecordingAudio() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }

        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy_h:mm a", Locale.getDefault())
        val date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            } catch (e: IOException) {

            }

            start()
        }

        binding.record.setImageResource(R.drawable.ic_baseline_pause)
        isRecording = true
        isPaused = false

        timer.start()

        binding.delete.isClickable = true
        binding.delete.setImageResource(R.drawable.ic_baseline_clear_pause)

        binding.recordList.visibility = View.GONE
        binding.save.visibility = View.VISIBLE
    }

    //Function to stop the recording
    private fun stopRecording() {
        timer.stop()

        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording = false

        binding.recordList.visibility = View.VISIBLE
        binding.save.visibility = View.GONE
        binding.delete.isClickable = false
        binding.delete.setImageResource(R.drawable.ic_baseline_clear_pause)
        binding.record.setImageResource(R.drawable.ic_play_pause)
        binding.timer.text = "00:00:00"

        amplitude = binding.recordingWave.clear()
    }

    //Override function to bind the time and wave form canvas while recording
    override fun onTimerTick(duration: String) {
        Log.i("Duration", duration)
        binding.timer.text = duration
        this.duration = duration.dropLast(3)
        binding.recordingWave.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}





