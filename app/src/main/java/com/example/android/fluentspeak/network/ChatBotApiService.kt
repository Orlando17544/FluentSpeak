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
    "https://api.writesonic.com"

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
        "X-API-KEY: 2a40c20f-7a0e-4f35-87b2-912ac6354465",
        "accept: application/json",
        "content-type: application/json"
    )
    @POST("v2/business/content/chatsonic?engine=premium&language=en")
    fun getResponse(@Body requestData: RequestData):
            Call<ChatBotResponse>
}

object ChatBotApi {
    val retrofitService : ChatBotApiService by lazy {
        retrofit.create(ChatBotApiService::class.java) }
}