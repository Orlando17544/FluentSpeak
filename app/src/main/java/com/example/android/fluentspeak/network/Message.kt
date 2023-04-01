package com.example.android.fluentspeak.network

import com.example.android.fluentspeak.MESSAGE_ROLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val role: Enum<MESSAGE_ROLE>,
    var content: String
)
