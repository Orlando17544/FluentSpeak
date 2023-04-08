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
        "Authorization: Bearer ${ApiKeys.OPEN_AI_API_KEY}"
    )
    @Multipart
    @POST("v1/audio/transcriptions")
    @JvmSuppressWildcards
    suspend fun getTranscriptionResponse(
        @PartMap transcriptionParams: Map<String, RequestBody>
    ): TranscriptionResponse

    @Headers(
        "Host: api.openai.com",
        "Authorization: Bearer ${ApiKeys.OPEN_AI_API_KEY}"
    )
    @Multipart
    @POST("v1/audio/translations")
    @JvmSuppressWildcards
    suspend fun getTranslationResponse(
        @PartMap translationParams: Map<String, RequestBody>
    ): TranslationResponse

    @Headers(
        "Host: api.openai.com",
        "Authorization: Bearer ${ApiKeys.OPEN_AI_API_KEY}"
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletionResponse(@Body chatCompletionRequestData: ChatCompletionRequestData): ChatCompletionResponse
}

object OpenAIApi: IOpenAIApi {
    val retrofitService : OpenAIApiService by lazy {
        retrofit.create(OpenAIApiService::class.java) }

    override suspend fun getTranscriptionResponse(transcriptionParams: Map<String, RequestBody>): TranscriptionResponse {
        return retrofitService.getTranscriptionResponse(transcriptionParams)
    }

    override suspend fun getTranslationResponse(translationParams: Map<String, RequestBody>): TranslationResponse {
        return retrofitService.getTranslationResponse(translationParams)
    }

    override suspend fun getChatCompletionResponse(chatCompletionRequestData: ChatCompletionRequestData): ChatCompletionResponse {
        return retrofitService.getChatCompletionResponse(chatCompletionRequestData)
    }
}