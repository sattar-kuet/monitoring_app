package com.example.callcenter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_calls")
data class MissedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromNumber: String,
    val dateTime: String,
    val synced: Boolean = false
)

@Entity(tableName = "received_calls")
data class ReceivedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromNumber: String,
    val delayMs: Long,
    val fileBase64: String,
    val dateTime: String,
    val synced: Boolean = false
)