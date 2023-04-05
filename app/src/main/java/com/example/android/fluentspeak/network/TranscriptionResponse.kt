package com.example.android.fluentspeak.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhisperResponse (
        val text: String
        )