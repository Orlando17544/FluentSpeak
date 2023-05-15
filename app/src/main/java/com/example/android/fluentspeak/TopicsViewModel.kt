package com.example.android.fluentspeak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.database.RedditDatabaseDao
import kotlinx.coroutines.launch

class TopicsViewModel(val database: RedditDatabaseDao): ViewModel() {
    val data = database.getConversationsWithUtterances("anime")
}

@Suppress("UNCHECKED_CAST")
class TopicsViewModelFactory (
    private val dataSource: RedditDatabaseDao
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (TopicsViewModel(dataSource) as T)
}