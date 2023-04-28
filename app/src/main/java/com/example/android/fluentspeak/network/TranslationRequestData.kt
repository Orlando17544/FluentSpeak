package com.example.android.fluentspeak.network

import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class TranslationRequestData(
    val file: File,
    val model: String = "whisper-1",
    val prompt: String = "",
    val responseFormat: String = "json",
    val temperature: Float = 0.0f
)
