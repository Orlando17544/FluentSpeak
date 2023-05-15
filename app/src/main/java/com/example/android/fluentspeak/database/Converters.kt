package com.example.android.fluentspeak.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let {
            val milliseconds = it * 1000
            Date(milliseconds)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        val seconds = date?.time?.toLong()?.div(1000)
        return seconds
    }
}