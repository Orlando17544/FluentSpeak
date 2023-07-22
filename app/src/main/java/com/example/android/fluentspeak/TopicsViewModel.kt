package com.example.android.fluentspeak

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.fluentspeak.database.Conversation
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.database.RedditDatabaseDao
import com.example.android.fluentspeak.network.ChatCompletionRequestData
import com.example.android.fluentspeak.network.ChatCompletionResponse
import com.example.android.fluentspeak.network.Choice
import com.example.android.fluentspeak.network.Usage
import kotlinx.coroutines.launch

class TopicsViewModel(val database: RedditDatabaseDao): ViewModel() {
    suspend fun getConversationsWithUtterances(subreddit: String): List<ConversationWithUtterances> {
        return database.getConversationsWithUtterances(subreddit)
    }

    val subreddits = database.getSubreddits()

    /*
    fun getSubreddits(): LiveData<List<String>> {
        val result = MutableLiveData<List<String>>();

        viewModelScope.launch {
            val subreddits = database.getSubreddits()

            result.value = subreddits
        }

        return result;
    }*/
}

@Suppress("UNCHECKED_CAST")
class TopicsViewModelFactory (
    private val dataSource: RedditDatabaseDao
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (TopicsViewModel(dataSource) as T)
}