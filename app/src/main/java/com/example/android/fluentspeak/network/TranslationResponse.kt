package com.example.android.fluentspeak.network

import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class TranslationResponse(
    val text: String
)
