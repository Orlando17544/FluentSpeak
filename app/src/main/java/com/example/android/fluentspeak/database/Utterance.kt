package com.example.android.fluentspeak.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utterance_table")
data class Utterance(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "utterance_id")
    val utteranceId: Long = 0L,

    @ColumnInfo(name = "timestamp")
    val timestamp: Int = -1,

    @ColumnInfo(name = "text")
    val text: String = "",

    @ColumnInfo(name = "speaker")
    val speaker: String = "",

    @ColumnInfo(name = "reply_to")
    val replyTo: String? = "",

    @ColumnInfo(name = "conversation_id")
    val conversationId: Long = 0L,

    @ColumnInfo(name = "meta_score")
    val metaScore: Int = -1,

    @ColumnInfo(name = "meta_top_level_comment")
    val metaTopLevelComment: String? = "",

    @ColumnInfo(name = "meta_retrieved_on")
    val metaRetrievedOn: Int = -1,

    @ColumnInfo(name = "meta_gilded")
    val metaGilded: Int = -1,

    @ColumnInfo(name = "meta_subreddit")
    val metaSubreddit: String = "",

    @ColumnInfo(name = "meta_stickied")
    val metaStickied: Boolean = false,

    @ColumnInfo(name = "meta_permalink")
    val metaPermalink: String = "",

    @ColumnInfo(name = "meta_author_flair_text")
    val metaAuthorFlairText: String = "",
)
