package com.example.android.fluentspeak.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequestData(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Float = 0.0f,

    @Json(name = "top_p")
    val topP: Float = 1f,
    val n: Int = 1,
    val stream: Boolean = false,
    val stop: String? = null,

    @Json(name = "max_tokens")
    val maxTokens: Int = 0,

    @Json(name = "presence_penalty")
    val presencePenalty: Float = 0.0f,

    @Json(name = "frequency_penalty")
    val frequencyPenalty: Float = 0.0f,

    @Json(name = "logit_bias")
    val logitBias: Map<String, Int>? = null,

    val user: String = "fluentSpeak"
)