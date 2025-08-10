package com.example.callcenter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CallDao {
    @Insert suspend fun insertMissedCall(call: MissedCallEntity)
    @Insert suspend fun insertReceivedCall(call: ReceivedCallEntity)
    @Query("SELECT * FROM missed_calls WHERE synced = 0") suspend fun getUnsyncedMissed(): List<MissedCallEntity>
    @Query("SELECT * FROM received_calls WHERE synced = 0") suspend fun getUnsyncedRecv(): List<ReceivedCallEntity>
    @Query("UPDATE missed_calls SET synced = 1 WHERE id = :id") suspend fun markMissedSynced(id: Int)
    @Query("UPDATE received_calls SET synced = 1 WHERE id = :id") suspend fun markRecvSynced(id: Int)
}