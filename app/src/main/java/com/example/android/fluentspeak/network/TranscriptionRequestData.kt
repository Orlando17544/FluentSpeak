package com.example.android.fluentspeak.network

import android.content.Context
import com.squareup.moshi.JsonClass
import java.io.File
import com.example.android.fluentspeak.FluentSpeakApplication.Companion.applicationContext
import com.example.android.fluentspeak.R

private val sharedPref = applicationContext().getSharedPreferences(applicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)

@JsonClass(generateAdapter = true)
data class TranscriptionRequestData(
    val file: File,
    val model: String = "whisper-1",
    val prompt: String = "",
    val responseFormat: String = "json",
    val temperature: Float = sharedPref.getFloat(applicationContext().getString(R.string.whisper_temperature_key), 0.0f),
    val language: String = "en"
)
