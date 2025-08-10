package com.example.callcenter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Util {
    fun currentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}