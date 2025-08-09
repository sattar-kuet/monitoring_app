package com.example.callcenter

data class CallEvent(
    val type: String,
    val number: String?,
    val time: Long,
    val extra: Long
)