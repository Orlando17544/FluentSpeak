package com.example.android.fluentspeak.network

import android.content.Context
import com.example.android.fluentspeak.FluentSpeakApplication.Companion.applicationContext
import com.example.android.fluentspeak.R
import com.squareup.moshi.JsonClass

private val sharedPref = applicationContext().getSharedPreferences(applicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)

@JsonClass(generateAdapter = true)
data class TextToSpeechRequestData(
    val input: Input,
    val voice: Voice = Voice(),
    val audioConfig: AudioConfig = AudioConfig()
)

@JsonClass(generateAdapter = true)
data class Input(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Voice(
    val languageCode: String = sharedPref.getString(applicationContext().getString(R.string.text_to_speech_accent_key), "").toString(),
    val name: String = sharedPref.getString(applicationContext().getString(R.string.text_to_speech_voice_name_key), "").toString(),
    val ssmlGender: String = sharedPref.getString(applicationContext().getString(R.string.text_to_speech_gender_key), "").toString()
)

@JsonClass(generateAdapter = true)
data class AudioConfig(
    val audioEncoding: String = "MP3"
)