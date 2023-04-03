package com.example.android.fluentspeak.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
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

interface OpenAIApiService {
    @Headers(
        "Host: api.openai.com",
        "Authorization: Bearer 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"
    )
    @Multipart
    @POST("v1/audio/transcriptions")
    @JvmSuppressWildcards
    suspend fun getWhisperResponse(
        @PartMap whisperParams: Map<String, RequestBody>
    ): WhisperResponse

    @Headers(
        "Host: api.openai.com",
        "Authorization: Bearer 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"
    )
    @POST("v1/chat/completions")
    suspend fun getChatGPTResponse(@Body chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse
}

object OpenAIApi: IOpenAIApi {
    val retrofitService : OpenAIApiService by lazy {
        retrofit.create(OpenAIApiService::class.java) }

    override suspend fun getWhisperResponse(whisperParams: Map<String, RequestBody>): WhisperResponse {
        return retrofitService.getWhisperResponse(whisperParams)
    }

    override suspend fun getChatGPTResponse(chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse {
        return retrofitService.getChatGPTResponse(chatGPTRequestData)
    }
}