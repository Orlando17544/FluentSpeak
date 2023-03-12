package com.example.android.fluentspeak.network

import com.squareup.moshi.Json

data class ChatGPTRequestData(
    @Json(name = "model")
    val model: String,

    @Json(name = "messages")
    val messages: MutableList<Message>,
)
