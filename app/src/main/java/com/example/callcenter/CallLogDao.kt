package com.example.callcenter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CallLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CallLogEntity)

    @Query("SELECT COUNT(*) FROM call_logs WHERE type = 'MISSED'")
    suspend fun getMissedCount(): Int

    @Query("SELECT COUNT(*) FROM call_logs WHERE type = 'RECEIVED'")
    suspend fun getReceivedCount(): Int

    @Query("SELECT delay FROM call_logs WHERE type = 'RECEIVED' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastReceivedDelay(): Int?
}