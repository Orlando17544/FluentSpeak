package com.example.android.fluentspeak.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranscriptionResponse (
        val text: String
        )