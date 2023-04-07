package com.example.android.fluentspeak.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


private const val BASE_URL =
    "https://texttospeech.googleapis.com"

private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.BODY)

private val client: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build();

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface GoogleCloudApiService {
    @Headers(
        "Host: texttospeech.googleapis.com",
        "Authorization: Bearer ${ApiKeys.GOOGLE_CLOUD_API_KEY}",
        "Content-Type: application/json; charset=utf-8"
    )
    @POST("v1/text:synthesize")
    suspend fun getTextToSpeechResponse(@Body textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse
}

object GoogleCloudApi: IGoogleCloudApi {
    val retrofitService : GoogleCloudApiService by lazy {
        retrofit.create(GoogleCloudApiService::class.java) }

    override suspend fun getTextToSpeechResponse(textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse {
        return retrofitService.getTextToSpeechResponse(textToSpeechRequestData)
    }
}