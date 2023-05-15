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

    @ColumnInfo(name = "num_comments")
    val numComments: Int = -1,

    @ColumnInfo(name = "domain")
    val domain: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Date,

    @ColumnInfo(name = "subreddit")
    val subreddit: String = "",

    @ColumnInfo(name = "gilded")
    val gilded: Int = -1,

    @ColumnInfo(name = "stickied")
    val stickied: Boolean = false,

    @ColumnInfo(name = "author_flair_text")
    val authorFlairText: String = ""
)
