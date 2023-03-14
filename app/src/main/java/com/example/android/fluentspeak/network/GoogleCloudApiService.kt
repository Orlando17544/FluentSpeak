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
        "Authorization: Bearer ya29.c.b0Aaekm1LmKEKJb6KDgx0zHsKqvLM2EZu5Ae3MnubeQhTdqngjnihrzJmESjp_w_kSkNkbhYOJq4fvjwFP7ViXTapdEgU6u5rIoE0EOyetXiEblbKz03EXSnws-AOjNg2iRt9VFaLYSrC93JDRO4Q140gpfSn1YBP_Vb2lWUCDn_LhAlgXRZEq-AgfSzpXDOAe-xoeDSE2Erajuiv65KxcSwiwBQwB61wMR8X_RZlM6f2qWaWzwAyMsIaywWpNX_Bng_GqhmHLxDGu10t28cggdwTnlPZkqLT7NPMiCAAOMCaCmYzFw9Ai77VBeox5rOy7klIdNpLYOAE339KR1Oj1qMFh3b_Sg7n58Byx_JxFY80nqBZRg11el-1sfzWe5cgxwgFi9lkV6pbuX4QsOpaoOV_wzcX9lVRFQh69fndOXOxRWYOq7WpWe5nfaluwymX7f_f8oVett6MtRwnBjxxzZidRe6YZV5-gQ0Wud9g4MIvtyJ7-Q3cRBXp6a90kt6cgMMmbI4Wpvgexsr2mxjXkSm_MBI4o5_gmIc7xOWVrsVJuWxvWlhrM2waisSzWb04vu7mw_Jov6icM2252xj-BqolevtRyV7gIp0ciXJUuS_bBn10xRpqBdJu_odSaIdJuZc_zyRk4wyaJIcWInvRpopp3ulQl9V3JOwX3Fg9b3Q-hlYOfk1dnYQqipZpxumu8Yvvwimbq6crMwo2Mepv8-JMUdn22RleYbbmORRaYF4ux8Mze_R7Z534ccWUQv_-l1z4Y2ezgBbbI3Qhww4Bw_9S8VU-frUowk7a10B90_8R6lWQ6bM02YX4BUqhiqO6yd6QjjbzIgsQ5hn0-1jo19akWWXBp_4VuupOjoagW0Xed4nBdgjSQUyzdFzcw55Bq3c-8iJhJ3S4cre84BgxhXjueXcu1y2Icjzzs2JFccRU00cem5yMuM1ghga1q-I5-6BX_b1061WUVoXnseVtrI57e08dMBQOu7S_Wyde2MpOaOWsre-yMale",
        "Content-Type: application/json; charset=utf-8"
    )
    @POST("v1/text:synthesize")
    suspend fun getTextToSpeechResponse(@Body textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse
}

object GoogleCloudApi {
    val retrofitService : GoogleCloudApiService by lazy {
        retrofit.create(GoogleCloudApiService::class.java) }
}