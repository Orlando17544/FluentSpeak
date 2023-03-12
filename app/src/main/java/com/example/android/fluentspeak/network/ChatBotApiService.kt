package com.example.android.fluentspeak.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


private const val BASE_URL =
    "https://api.openai.com"

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

interface ChatBotApiService {
    @Headers(
        "Host: api.openai.com",
        "Authorization: Bearer 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"
    )
    @Multipart
    @POST("v1/audio/transcriptions")
    fun getResponse(
        @Part("file\"; filename=\"recording.m4a\"") file: RequestBody,
        @Part("model") model: RequestBody
    ): Call<ChatBotResponse?>?
/*
    @Multipart
    @POST("v1/audio/transcriptions")
    fun getResponse(
        @Part filePart: MultipartBody.Part,
        @Part textPart: MultipartBody.Part
    ): Call<ChatBotResponse?>?*/
/*
    @Multipart
    @POST("v1/audio/transcriptions")
    fun getResponse(@Body chatBotRequestData: ChatBotRequestData):
            Call<ChatBotResponse>*/
}

object ChatBotApi {
    val retrofitService : ChatBotApiService by lazy {
        retrofit.create(ChatBotApiService::class.java) }
}