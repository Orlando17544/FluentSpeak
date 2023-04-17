package com.example.android.fluentspeak.network

import android.content.Context
import com.example.android.fluentspeak.FluentSpeakApplication.Companion.applicationContext
import com.example.android.fluentspeak.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

private val sharedPref = applicationContext().getSharedPreferences(applicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequestData(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Float = sharedPref.getFloat(applicationContext().getString(R.string.chat_gpt_temperature_key), 0.0f),

    @Json(name = "top_p")
    val topP: Float = 1f,
    val n: Int = 1,
    val stream: Boolean = false,
    val stop: String? = null,

    @Json(name = "max_tokens")
    val maxTokens: Int = sharedPref.getInt(applicationContext().getString(R.string.chat_gpt_max_tokens_key), 0),

    @Json(name = "presence_penalty")
    val presencePenalty: Float = sharedPref.getFloat(applicationContext().getString(R.string.chat_gpt_presence_penalty_key), 0.0f),

    @Json(name = "frequency_penalty")
    val frequency_penalty: Float = sharedPref.getFloat(applicationContext().getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f),

    @Json(name = "logit_bias")
    val logitBias: Map<String, Int>? = null,

    val user: String = "fluentSpeak"
)