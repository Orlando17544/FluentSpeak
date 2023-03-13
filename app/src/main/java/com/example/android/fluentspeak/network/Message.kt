package com.example.android.fluentspeak.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)
