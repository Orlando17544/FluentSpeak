package com.example.android.fluentspeak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.database.RedditDatabaseDao

class MainViewModel : ViewModel() {
    lateinit var conversationsWithUtterances: List<ConversationWithUtterances>


}

/*
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory (
    private val dataSource: RedditDatabaseDao
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (MainViewModel(dataSource) as T)
}*/