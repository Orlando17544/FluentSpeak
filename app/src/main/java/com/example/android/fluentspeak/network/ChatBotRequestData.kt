package com.example.android.fluentspeak.network

import com.squareup.moshi.Json

data class ChatBotRequestData(
    @Json(name = "enable_google_results")
    val enableGoogleResults: Boolean?,

    @Json(name = "enable_memory")
    val enableMemory: Boolean?,

    @Json(name = "input_text")
    val inputText: String?
)
