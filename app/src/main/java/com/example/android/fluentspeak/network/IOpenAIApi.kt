package com.example.android.fluentspeak.network

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PartMap

interface IOpenAIApi {
    suspend fun getTranscriptionResponse(transcriptionParams: Map<String, RequestBody>): TranscriptionResponse

    suspend fun getTranslationResponse(translationParams: Map<String, RequestBody>): TranslationResponse

    suspend fun getChatCompletionResponse(chatCompletionRequestData: ChatCompletionRequestData): ChatCompletionResponse
}