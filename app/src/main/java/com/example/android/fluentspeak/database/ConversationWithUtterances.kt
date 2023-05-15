package com.example.android.fluentspeak.database

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithUtterances(
    @Embedded val conversation: Conversation,
    @Relation(
        parentColumn = "conversation_id",
        entityColumn = "conversation_id"
    )
    val utterances: List<Utterance>
)
