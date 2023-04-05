package com.example.android.fluentspeak.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String,

    @Json(name = "object")
    val _object: String,

    val created: Int,
    val choices: List<Choice>,
    val usage: Usage
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: Message,

    @Json(name = "finish_reason")
    val finishReason: String,
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens")
    val promptTokens: Int,

    @Json(name = "completion_tokens")
    val completionTokens: Int,

    @Json(name = "total_tokens")
    val totalTokens: Int
    )