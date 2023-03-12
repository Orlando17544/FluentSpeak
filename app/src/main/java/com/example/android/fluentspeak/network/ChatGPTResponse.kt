package com.example.android.fluentspeak.network

import com.squareup.moshi.Json

data class ChatGPTResponse(
    @Json(name = "message")
    val message: Message
)
