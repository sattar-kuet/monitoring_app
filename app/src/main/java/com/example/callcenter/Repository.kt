package com.example.callcenter

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(
    private val db: AppDatabase,
    private val api: ApiService,
    private val context: Context
) {

    companion object {
        private const val TAG = "SYNC_DEBUG"
    }

    /** ✅ Missed Call Save */
    suspend fun saveMissedCall(fromNumber: String, dateTime: String) {
        withContext(Dispatchers.IO) {
            val net = NetworkManager.isConnected(context)
            Log.d(TAG, "saveMissedCall() Network=$net, Number=$fromNumber")

            if (net) {
                try {
                    val resp = api.sendMissedCall(
                        ParamsWrapper(
                            MissedCallParams(
                                from_number = fromNumber,
                                date_time = dateTime
                            )
                        )
                    )
                    if (resp.isSuccessful) {
                        Log.d(TAG, "✅ Missed Call posted successfully.")
                        return@withContext
                    } else {
                        Log.e(TAG, "❌ Missed Call API failed: ${resp.code()} ${resp.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception posting missed call: ${e.message}")
                }
            }
            db.callDao().insertMissedCall(
                MissedCallEntity(fromNumber = fromNumber, dateTime = dateTime)
            )
            Log.d(TAG, "💾 Missed save offline.")
        }
    }

    /** ✅ Received Call Save */
    suspend fun saveReceivedCall(fromNumber: String, delay: Long, fileBase64: String, dateTime: String) {
        withContext(Dispatchers.IO) {
            val net = NetworkManager.isConnected(context)
            Log.d(TAG, "saveReceivedCall() Network=$net, Number=$fromNumber")

            if (net) {
                try {
                    val resp = api.saveCallRecord(
                        ParamsWrapper(
                            ReceivedCallParams(
                                from_number = fromNumber,
                                delay = delay,
                                file = fileBase64,
                                date_time = dateTime
                            )
                        )
                    )
                    if (resp.isSuccessful) {
                        Log.d(TAG, "✅ Received Call posted successfully.")
                        return@withContext
                    } else {
                        Log.e(TAG, "❌ Received Call API failed: ${resp.code()} ${resp.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception posting received: ${e.message}")
                }
            }
            db.callDao().insertReceivedCall(
                ReceivedCallEntity(
                    fromNumber = fromNumber,
                    delayMs = delay,
                    fileBase64 = fileBase64,
                    dateTime = dateTime
                )
            )
            Log.d(TAG, "💾 Received save offline.")
        }
    }

    /** ✅ Sync Offline Data */
    suspend fun syncOfflineData() {
        withContext(Dispatchers.IO) {
            if (!NetworkManager.isConnected(context)) {
                Log.w(TAG, "No internet ❌ — skipping sync")
                return@withContext
            }

            val unsyncedMissed = db.callDao().getUnsyncedMissed()
            Log.d(TAG, "Found ${unsyncedMissed.size} unsynced missed calls.")
            for (mc in unsyncedMissed) {
                try {
                    val resp = api.sendMissedCall(
                        ParamsWrapper(
                            MissedCallParams(
                                from_number = mc.fromNumber,
                                date_time = mc.dateTime
                            )
                        )
                    )
                    if (resp.isSuccessful) {
                        db.callDao().markMissedSynced(mc.id)
                        Log.d(TAG, "✅ Missed synced: ${mc.fromNumber}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception syncing missed: ${e.message}")
                }
            }

            val unsyncedRecv = db.callDao().getUnsyncedRecv()
            Log.d(TAG, "Found ${unsyncedRecv.size} unsynced received calls.")
            for (rc in unsyncedRecv) {
                try {
                    val resp = api.saveCallRecord(
                        ParamsWrapper(
                            ReceivedCallParams(
                                from_number = rc.fromNumber,
                                delay = rc.delayMs,
                                file = rc.fileBase64,
                                date_time = rc.dateTime
                            )
                        )
                    )
                    if (resp.isSuccessful) {
                        db.callDao().markRecvSynced(rc.id)
                        Log.d(TAG, "✅ Received synced: ${rc.fromNumber}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception syncing received: ${e.message}")
                }
            }
        }
    }
}