package com.example.callcenter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class CallService : Service() {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, "call_recording_channel")
            .setContentTitle("Call Recording")
            .setContentText("Recording call in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        try {
            val dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (dir != null && (dir.exists() || dir.mkdirs())) {
                file = File(dir, "call_${System.currentTimeMillis()}.3gp")
                Log.d("CallService", "Recording file: ${file!!.absolutePath}")
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(file!!.absolutePath)
                    prepare()
                    start()
                }
            } else {
                Log.e("CallService", "Directory not available for recording!")
            }
        } catch (e: Exception) {
            Log.e("CallService", "startRecording error", e)
        }
    }

    override fun onDestroy() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e("CallService", "Recorder stop error", e)
        }
        recorder?.release()
        recorder = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "call_recording_channel",
                "Call Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}