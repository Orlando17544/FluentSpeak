package com.example.android.fluentspeak

import com.example.android.fluentspeak.network.Voice

data class Speaker(
    val name: String,
    val gender: String,
    val textColor: String,
    val voice: Voice
)