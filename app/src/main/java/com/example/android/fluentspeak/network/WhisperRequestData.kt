package com.example.android.fluentspeak.network

import com.squareup.moshi.Json

data class WhisperRequestData(
    @Json(name = "version")
    val version: String?,

    @Json(name = "enable_memory")
    val enableMemory: Boolean?,

    @Json(name = "input_text")
    val inputText: String?
)
