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
        "Authorization: Bearer ya29.c.b0Aaekm1IWwK75xm_NGYKSj1cM9fHVOGkuHqOCGvYI9ei_cwQongoOTrA-bw8LAoR1cvi-BLMeTY8DL8bzxx3FzJiVlY0rZwoLVSH_0x2oFxgQZgpM7E8mEJHoYFQVnDGwObgyCY9vgtkmztoDhU8fbSe4yJp1EG0sazHgXPwFy4dD3aMqKKSgygIrXD2kWjhSquiZF5LrIuUy0I6exMl8DhW3lU2mn_RHksVtcxRpEbNmbUk9twob0VQlOgfnJGTF3t9jzuTqh6rhwnJsKO-CbvFrBynGhonwJBSR8cS-IBEMFVWF-T6Bbp9DI9wtPAwPpDRZ-XoiGQG339A35nyhI-0tS4WqZWF8hFab4UxJ_okvZFbV7rk3n1gQnMVi9ukz8I2Yp7llWgrBj9xkOdjs_rBk_YMBapBIRt68JZRymBIu6-p3nI0sbuj439207kq4XsQfYmf45xh9ji6_VQuhnbWSff9Flkb7nFkM7ct5f6odjS-sjqO7M8Vvk_tUrB57Fax8oVhMsncwVm07yIZbhJe0BykyvVflaihvUfOwdJw3MYZfmUFmi2_zOi1ZU9jaubMtvlibQlWR7V9nmadB0Uxhvt5zysRB_Y0z2rkwSdpbvOp5_QgQrqhJoBqxSeQiiRRg1UMyyfXU6dduy2a8uvjbBcidBaOiFbchUd5YU8aZok01oiSm_nnUvs1IcS3JJigdhqm6l3qkvIdd66quwXbQgrw82Zv1Os0fRgnR5l1kkOjXtxmoc2OqqOnZ6-F6nhaW9e_3MnSpQdxjax1ecfa3XQU7MXnzzIp-jOxrudnfoRW5Q2QiqF-uU02mt-n_fvjajtpXByYhz1f57B7_o60lJYwMffIS6u7Y-2h_amRzBhp59wBnSuYQ1t2Z2O2cJm1F__11r39408y0FMnner9pO9ubuejYkuf4frp7UB5Q7f_sYaO84hoJY1fnnmj-bmIzW4bXBguJZJ93pxXZ1_ZVqBU5iRnUgJJUam05BsYcUkV7VrYOhzm",
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