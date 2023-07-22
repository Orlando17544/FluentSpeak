package com.example.android.fluentspeak.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RedditDatabaseDao {
    @Transaction
    @Query("SELECT * FROM conversation_table WHERE subreddit = :subreddit ORDER BY RANDOM()")
    suspend fun getConversationsWithUtterances(subreddit: String): List<ConversationWithUtterances>

    @Query("SELECT DISTINCT subreddit FROM conversation_table")
    fun getSubreddits(): LiveData<List<String>>
}