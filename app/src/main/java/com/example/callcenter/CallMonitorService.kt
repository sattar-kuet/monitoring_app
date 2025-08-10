package com.example.callcenter

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallMonitorService : Service() {

    private var telephonyManager: TelephonyManager? = null
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var callStartTime = 0L
    private var incomingNumber: String? = null

    override fun onCreate() {
        super.onCreate()
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CALL_MONITOR", "READ_PHONE_STATE permission missing. Can't listen.")
            stopSelf()
            return
        }

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            val db = AppDatabase.getInstance(this@CallMonitorService)
            val repo = Repository(db, ApiClient.apiService, this@CallMonitorService)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    incomingNumber = phoneNumber
                    callStartTime = System.currentTimeMillis()
                    lastState = state
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    lastState = state
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        // Missed Call
                        val time = Util.currentDateTime()
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.saveMissedCall(incomingNumber ?: "Unknown", time)
                        }
                    } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        // Received Call
                        val delay = System.currentTimeMillis() - callStartTime
                        val time = Util.currentDateTime()
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.saveReceivedCall(incomingNumber ?: "Unknown", delay, "file_base64_data", time)
                        }
                    }
                    lastState = state
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun getNotification(): Notification {
        val channelId = "call_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Call Monitor", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Call Monitor Running")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .build()
    }
}