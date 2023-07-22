package com.example.android.fluentspeak.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "conversation_table")
data class Conversation(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "subreddit")
    val subreddit: String = "",
)
