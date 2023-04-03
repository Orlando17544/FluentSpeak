package com.example.android.fluentspeak.network

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PartMap

interface IOpenAIApi {
    suspend fun getWhisperResponse(@PartMap whisperParams: Map<String, RequestBody>): WhisperResponse

    suspend fun getChatGPTResponse(@Body chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse
}