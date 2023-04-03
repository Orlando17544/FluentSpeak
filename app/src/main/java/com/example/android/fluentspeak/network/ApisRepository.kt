package com.example.android.fluentspeak.network

data class ApisRepository(
    val openAIApi: IOpenAIApi,
    val googleCloudApi: IGoogleCloudApi
)