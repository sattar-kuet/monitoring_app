package com.example.callcenter

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log

class CallDetectService : Service() {

    private lateinit var telephonyManager: TelephonyManager

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)

                Log.d("CALL_DETECT", "State=$state Number=$phoneNumber")

                val db = AppDatabase.getInstance(this@CallDetectService)
                val repo = Repository(db, ApiClient.apiService, this@CallDetectService)

                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}