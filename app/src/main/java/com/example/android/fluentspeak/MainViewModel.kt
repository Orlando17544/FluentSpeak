package com.example.android.fluentspeak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.fluentspeak.database.ConversationWithUtterances

class MainViewModel : ViewModel() {

    private val _conversations = MutableLiveData<List<ConversationWithUtterances>>()
    val conversations: LiveData<List<ConversationWithUtterances>> = _conversations

    fun setConversations(conversations: List<ConversationWithUtterances>) {
        _conversations.value = conversations
    }
}