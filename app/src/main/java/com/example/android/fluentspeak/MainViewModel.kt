package com.example.android.fluentspeak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.fluentspeak.database.RedditDatabaseDao

class MainViewModel(val database: RedditDatabaseDao) : ViewModel() {
    //val conversations = database.getConversationsWithUtterances("anime")
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory (
    private val dataSource: RedditDatabaseDao
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (MainViewModel(dataSource) as T)
}