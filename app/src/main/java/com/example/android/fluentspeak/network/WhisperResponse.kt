package com.example.android.fluentspeak.network

import com.squareup.moshi.Json

data class WhisperResponse (
        @Json(name = "text")
        val text: String
        )