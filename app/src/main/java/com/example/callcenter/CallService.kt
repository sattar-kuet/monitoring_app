package com.example.callcenter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallService : Service() {

    private var recorder: MediaRecorder? = null
    private lateinit var outputFile: String
    private var number: String = ""
    private var delay: Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        number = intent?.getStringExtra("number") ?: ""
        delay = intent?.getLongExtra("delay", 0) ?: 0

        createNotificationChannel()
        startForeground(1, getNotification())

        startRecording()

        return START_NOT_STICKY
    }

    private fun startRecording() {
        try {
            val fileName = "call_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp3"
            outputFile = File(getExternalFilesDir(null), fileName).absolutePath

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder?.release()
        recorder = null

        val audioFile = File(outputFile)
        if (audioFile.exists() && audioFile.length() > 0) {
            val fileBytes = audioFile.readBytes()
            val fileBase64 =
                android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP)

            val db = AppDatabase.getInstance(this)
            val repo = Repository(db, ApiClient.apiService, this)

            val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .apply { timeZone = TimeZone.getDefault() }
                .format(Date())

            CoroutineScope(Dispatchers.IO).launch {
                repo.saveReceivedCall(number, delay, fileBase64, dateTime)
            }
        } else {
            Log.e("CALL_SERVICE", "Recorded file missing or empty!")
        }
    }

    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, "call_service_channel")
            .setContentTitle("Recording Call")
            .setContentText("Recorder running...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "call_service_channel",
                "Call Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}