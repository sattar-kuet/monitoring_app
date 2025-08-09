package com.example.callcenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {
    companion object {
        var ringStart: Long = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
        val db = CallLogDatabase.getDatabase(context)
        val dao = db.callLogDao()

        if (TelephonyManager.EXTRA_STATE_RINGING == state) {
            ringStart = System.currentTimeMillis()
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK == state) {
            val delay = ((System.currentTimeMillis() - ringStart) / 1000).toInt()
            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(
                    CallLogEntity(
                        number = number,
                        type = "RECEIVED",
                        delay = delay,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            // Start recording
            context.startForegroundService(Intent(context, CallService::class.java))
        } else if (TelephonyManager.EXTRA_STATE_IDLE == state) {
            if (ringStart > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insert(
                        CallLogEntity(
                            number = number,
                            type = "MISSED",
                            delay = 0,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                ringStart = 0
            }
        }
    }
}