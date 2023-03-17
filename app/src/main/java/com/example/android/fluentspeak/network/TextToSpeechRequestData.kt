package com.example.android.fluentspeak.network

import com.squareup.moshi.JsonClass

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
    val languageCode: String = "en-IN",
    val name: String = "en-IN-Standard-A",
    val ssmlGender: String = "FEMALE"
)

@JsonClass(generateAdapter = true)
data class AudioConfig(
    val audioEncoding: String = "MP3"
)