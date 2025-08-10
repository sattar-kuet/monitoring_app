package com.example.todoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.telephony.TelephonyManager
import com.example.callcenter.ApiClient
import com.example.callcenter.AppDatabase
import com.example.callcenter.CallService
import com.example.callcenter.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallReceiver : BroadcastReceiver() {

    private var lastState: String? = null
    private var incomingNumber: String? = null
    private var ringTime: Long = 0

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    incomingNumber = number
                    ringTime = System.currentTimeMillis()
                    lastState = TelephonyManager.EXTRA_STATE_RINGING
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    lastState = TelephonyManager.EXTRA_STATE_OFFHOOK
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    when (lastState) {
                        TelephonyManager.EXTRA_STATE_RINGING -> {
                            // Missed call case
                            checkForMissedCall(context)
                        }
                        TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                            // Received call case
                            val delay = System.currentTimeMillis() - ringTime
                            postReceivedCall(context, incomingNumber ?: "Unknown", delay)
                        }
                    }
                    lastState = TelephonyManager.EXTRA_STATE_IDLE
                }
            }
        }
    }

    private fun checkForMissedCall(context: Context) {
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
                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date))

                if (type == CallLog.Calls.MISSED_TYPE) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getInstance(context)
                        val repo = Repository(db, ApiClient.apiService, context)
                        repo.saveMissedCall(number ?: "Unknown", dateTime)
                    }
                }
            }
        }
    }

    private fun postReceivedCall(context: Context, number: String, delay: Long) {
        val intent = Intent(context, CallService::class.java).apply {
            putExtra("number", number)
            putExtra("delay", delay)
        }
        context.startForegroundService(intent)
    }
}