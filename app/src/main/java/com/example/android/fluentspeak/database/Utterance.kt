package com.example.android.fluentspeak.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utterance_table")
data class Utterance(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "utterance_id")
    val utteranceId: Long = 0L,

    @ColumnInfo(name = "text")
    val text: String = "",

    @ColumnInfo(name = "speaker")
    val speaker: String = "",

    @ColumnInfo(name = "gender")
    val gender: String = "",

    @ColumnInfo(name = "reply_to")
    val replyTo: String? = "",

    @ColumnInfo(name = "conversation_id")
    val conversationId: Long = 0L,
)
