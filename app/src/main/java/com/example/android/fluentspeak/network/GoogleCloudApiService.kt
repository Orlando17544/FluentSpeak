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
        "Authorization: Bearer ya29.c.b0Aaekm1LbwZRBNBAkVd8BUpvOQ1DO0JxRq0i4HAQXKTzRzPrNhp5Q2bKJh34a6Mm_t52h1P-XJyWXTbEVLarx01qbpmSuw4OB4Vg9e5BAt5Hwj8HZ5-LmETcGQwbSKkduzTH5i-VmwZUpg3X059EPOk-j30w7l5ASHHGeIg2YNykHyP_GK_n5Z4ghvPp4HaBr4zktVenjp2vanvq22sTzArONt7YsDs8D-zdgHIPdDu9wAZ5rUJ3UhXuktN4KzOjKNJqEx-jd3du1HMIKvZSwnUn1Tp_CwFoSv9GZXmWDuNqFyOeR6v31NQwPSMncp7WFe9nLjCdtOhIG340KkVathhj-kOXab3rYUkw125JQta4X3F2ReOuU4bRztrgh2Rrr8stjesOwX6thF3SnltSel30Y4qZ26Mxi9fn9hr_8rbisbv7iZVlRMiotIvFatkVmOskQor1Sm39nFM2jOzxg2zZO72R2MnB0r3hSxS_M3iBR91x-ptcXMSIzBhYVy1QOSigp_jmruR8ueM_tIaOQYvX_bzkYf1O6ZO4gk81thzIln_c-xvF4Fywrhhys7cymU8hxUifzhiiyXoo8slOzr0iq0zstWRhm34fh1bqh2rq1Qw9fYRd6ZdYkZzbSkgfJl5OIXMpn5xx0cs0w7qI3sIR772_MXdXlsSObp7uYQJibmZOwYYOJcU339XwJfmIniMk068mVaq9rYp-68u4-VcFkIttWpy65J573OVuslYrb-l-yzmeJmXZZjV9iez8upVV923BYjMb7bq3uyMkdbmRfOjBZ9gZ6oeombgIhu9jcU1BYWBl5m5q-eVdi0wzhFRz2IjyYwRJ6ac-U8ejyRR2b9M6943gI9sp5R8Z4aXMUQUo47eO4kpnySgJkUtOkkmXIi_6rfpmO0sjZjIdzcOo1yOw10o7F6iQpRjgWWOSIaayzzS35czWni_ya5nYFBQJXnI8hIIpFiFrZas5qhhXujsx7U5tdsXtwQIi9UvY5k754v09xiSX",
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