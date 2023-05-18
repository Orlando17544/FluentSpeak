package com.example.android.fluentspeak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.fluentspeak.database.ConversationWithUtterances

class MainViewModel : ViewModel() {

    private val _conversations = MutableLiveData<List<ConversationWithUtterances>>()
    val conversations: LiveData<List<ConversationWithUtterances>> = _conversations

    private val _previousConversations = MutableLiveData<List<ConversationWithUtterances>>()
    val previousConversations: LiveData<List<ConversationWithUtterances>> = _previousConversations

    fun setConversations(conversations: List<ConversationWithUtterances>) {
        _conversations.value = conversations
    }

    fun setPreviousConversations(conversations: List<ConversationWithUtterances>) {
        _previousConversations.value = conversations
    }
}