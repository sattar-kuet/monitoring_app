package com.example.callcenter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState: String? = TelephonyManager.EXTRA_STATE_IDLE
        private var callStartTime: Long = 0
        private var incomingNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("CALL_RECEIVER", "State=$stateStr Number=$number lastState=$lastState")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    incomingNumber = number
                    callStartTime = System.currentTimeMillis()
                    lastState = TelephonyManager.EXTRA_STATE_RINGING
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    lastState = TelephonyManager.EXTRA_STATE_OFFHOOK
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                        // Missed Call
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkForMissedCall(context)
                        }, 1500)
                    } else if (lastState == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        // Received Call
                        val delayTime = System.currentTimeMillis() - callStartTime
                        saveReceivedCall(context, incomingNumber ?: "Unknown", delayTime)
                    }
                    lastState = TelephonyManager.EXTRA_STATE_IDLE
                }
            }
        }
    }

    private fun checkForMissedCall(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CALL_RECEIVER", "No READ_CALL_LOG permission")
            return
        }

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null,
            CallLog.Calls.DATE + " DESC"
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))

                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .apply { timeZone = TimeZone.getDefault() }
                    .format(Date(date))

                if (type == CallLog.Calls.MISSED_TYPE) {
                    Log.d("CALL_RECEIVER", "Missed call: $number at $dateTime")
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getInstance(context)
                        val repo = Repository(db, ApiClient.apiService, context)
                        repo.saveMissedCall(number ?: "Unknown", dateTime)
                    }
                }
            }
        }
    }

    private fun saveReceivedCall(context: Context, number: String, delay: Long) {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .apply { timeZone = TimeZone.getDefault() }
            .format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val repo = Repository(db, ApiClient.apiService, context)
            repo.saveReceivedCall(number, delay, "file_base64_data", dateTime)
        }
    }
}