package com.example.android.fluentspeak.network

import android.content.Context
import com.example.android.fluentspeak.FluentSpeakApplication.Companion.applicationContext
import com.example.android.fluentspeak.R
import com.squareup.moshi.JsonClass
import java.io.File

private val sharedPref = applicationContext().getSharedPreferences(applicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)

@JsonClass(generateAdapter = true)
data class TranslationRequestData(
    val file: File,
    val model: String = "whisper-1",
    val prompt: String = "",
    val responseFormat: String = "json",
    val temperature: Float = sharedPref.getFloat(applicationContext().getString(R.string.whisper_temperature_key), 0.0f)
)
