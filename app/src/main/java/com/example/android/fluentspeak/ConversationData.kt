package com.example.android.fluentspeak

import com.example.android.fluentspeak.network.Message
import java.util.*

object ConversationData {
    public val messages: MutableList<Message> = mutableListOf()

    fun addMessage(message: Message) {
        if (messages.size.equals(5)) {
            //Don't remove system's message
            messages.removeAt(1)
        }

        messages.add(message)
    }

    fun cleanMessages() {
        messages.clear()
    }
}