package com.example.callcenter

data class ParamsWrapper<T>(
    val params: T
)

data class MissedCallParams(
    val from_number: String,
    val date_time: String
)

data class ReceivedCallParams(
    val from_number: String,
    val delay: Long,
    val file: String,
    val date_time: String
)
