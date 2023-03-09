package com.example.android.fluentspeak.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private const val BASE_URL =
    "https://api.replicate.com"

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

interface WhisperApiService {
    @Headers(
        "Authorization: Token 3f8c69d078c41c44e1a98629026516f07af2a199",
        "Content-Type: application/json"
    )
    @POST("v1/predictions")
    fun getResponse(@Body whisperRequestData: WhisperRequestData):
            Call<ChatBotResponse>
}

object WhisperApi {
    val retrofitService : WhisperApiService by lazy {
        retrofit.create(WhisperApiService::class.java) }
}