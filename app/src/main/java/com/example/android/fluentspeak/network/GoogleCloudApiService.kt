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
        "Authorization: Bearer ya29.c.b0Aaekm1I1TRWlxrc3Ig4cXpB8Oh00Sl8JAHt2gQGgMhZadRWInAaj6DAVLH76EtSdvQpnWAc0Dx3bGSbEhIefHXnDOVsLZ8XEw6zezFf3rq1R51YCktW5GSG4uS2S2xUUUKE7GM7YNaNibpkijJN2p2VhEvJQA4UeaIN1mkO2WdaYKURhXzcJ05IuakD3kBPaQUhgn3_Qi7ciElY9VxgGL6SPlJcXKJSVSkAGL6SFtbg2EysJnxFUvilv6LHhhNqcJtF72IbEq-CRi7KZz0ls1MEp94QJ9QciVvH-cZ8x5DTtlNlP9cvkGgFfG5YpH8XHjVsglcFlWwG339PMBcfkupy24w3Q2fpu0v4jnvrzQtBbZXy4lyXaVU0oyvriaU8gldWS_OfnBagX3kZU2QvBtYdf9uu8r1bIx_d44pYw3jq4J2FWxVM3juwkYtB739fei2Rs2gepVltO-umt-M27vewuSfwaj03I1Yd5OXslmjqQR__zMVcfiVJmt17bWxMkoFJtMbeMQVSzp9S-R9d4edF_l_Wxkdq8WviYw6VZ1jgRmpqRSVwBtYm8qwX4igy7ikInFe8vsi8BR3sbXRZX25XMt9UpwMgeZlc0F9l5nF2X9k4Bdh9z6gjOa9XSmbMqZ-OVB7Z5_wwXZ5cRkfS5UthV7JticqrJmgbi6Q-UBWbQcw0O28wmbZgXVRg8kqQs6SUu0XvF2OqWo8QeJVUO_60ke-QFW3SMUSzO317mUt74xgq5mU70Ubq5Oss2bU5ZvWaRrhJ8n24jdR48zh1W-uJqjQZQUl9lRgOBkZ_2ke_yjzZ5-1ez36h847nvU8uk8xQY3V14scBo0f8n1O6uh9om0xJ7w3fsrvry39ij2tFRkIkdBdvyROb7h23-0Iz6knnZust9sgzwOWFdFj8ciZxO6e7Y04MpV-8rmRaBc_-mSwhhizMMox1j7uJYmuakmiF-z8-fnxM8b5ZbcX3VSoB4d1xqvRp9053ZORyYyiyhpeWIpFUQIzn",
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