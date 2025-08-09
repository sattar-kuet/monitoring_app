package com.example.callcenter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: String,
    val type: String, // "MISSED" or "RECEIVED"
    val delay: Int, // seconds
    val timestamp: Long
)