package com.example.callcenter

import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.CallLog
import android.telephony.SubscriptionManager

object Utils {
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(network) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Get SIM-1's subscriptionId
    fun getSim1SubscriptionId(context: Context): Int {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val list = subscriptionManager.activeSubscriptionInfoList
        // SIM-1 is usually slotIndex 0
        return list?.firstOrNull { it.simSlotIndex == 0 }?.subscriptionId ?: -1
    }

    // Get missed call count for SIM-1
    fun getMissedCallCountSim1(context: Context, sim1SubId: Int): Int {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            "${CallLog.Calls.TYPE}=? AND subscription_id=?",
            arrayOf(CallLog.Calls.MISSED_TYPE.toString(), sim1SubId.toString()),
            null
        )
        val count = cursor?.count ?: 0
        cursor?.close()
        return count
    }

    // Get received call count for SIM-1
    fun getReceivedCallCountSim1(context: Context, sim1SubId: Int): Int {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            "${CallLog.Calls.TYPE}=? AND subscription_id=?",
            arrayOf(CallLog.Calls.INCOMING_TYPE.toString(), sim1SubId.toString()),
            null
        )
        val count = cursor?.count ?: 0
        cursor?.close()
        return count
    }

    // Get last received call delay for SIM-1 (in seconds)
    fun getLastReceivedCallDelaySim1(context: Context, sim1SubId: Int): Int {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            "${CallLog.Calls.TYPE}=? AND subscription_id=?",
            arrayOf(CallLog.Calls.INCOMING_TYPE.toString(), sim1SubId.toString()),
            "${CallLog.Calls.DATE} DESC"
        )
        var delay = 0
        if (cursor != null && cursor.moveToFirst()) {
            // Ring duration is not directly available in call log.
            // You may need to calculate from your own DB or use call start/end time if you log it.
            // Here, just showing call duration as a placeholder.
            delay = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
        }
        cursor?.close()
        return delay
    }
}