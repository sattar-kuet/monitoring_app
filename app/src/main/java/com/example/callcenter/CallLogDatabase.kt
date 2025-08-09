package com.example.callcenter

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallLogEntity::class], version = 1)
abstract class CallLogDatabase : RoomDatabase() {
    abstract fun callLogDao(): CallLogDao

    companion object {
        @Volatile
        private var INSTANCE: CallLogDatabase? = null

        fun getDatabase(context: Context): CallLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallLogDatabase::class.java,
                    "call_log_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}