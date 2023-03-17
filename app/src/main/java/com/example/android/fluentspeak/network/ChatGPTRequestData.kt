package com.example.android.fluentspeak.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatGPTRequestData(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Float = 1f,

    @Json(name = "top_p")
    val topP: Float = 1f,
    val n: Int = 1,
    val stream: Boolean = false,
    val stop: String? = null,

    @Json(name = "max_tokens")
    val maxTokens: Int = 100,

    @Json(name = "presence_penalty")
    val presencePenalty: Float = 0f,

    @Json(name = "frequency_penalty")
    val frequency_penalty: Float = 0f,

    @Json(name = "logit_bias")
    val logitBias: Map<String, Int>? = null,

    val user: String = "fluentSpeak"
)