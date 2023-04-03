package com.example.android.fluentspeak.network

import retrofit2.http.Body

interface IGoogleCloudApi {
    suspend fun getTextToSpeechResponse(@Body textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse
}