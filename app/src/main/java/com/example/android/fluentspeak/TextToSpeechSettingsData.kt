package com.example.android.fluentspeak

import com.example.android.fluentspeak.network.Voice

class TextToSpeechSettingsData {
    companion object {
        val VOICES = listOf<Voice>(
            Voice("en-AU", "en-AU-Neural2-A", "FEMALE"),
            Voice("en-AU", "en-AU-Neural2-B", "MALE"),
            Voice("en-AU", "en-AU-Neural2-C", "FEMALE"),
            Voice("en-AU", "en-AU-Neural2-D", "MALE"),
            Voice("en-AU", "en-AU-Standard-A", "FEMALE"),
            Voice("en-AU", "en-AU-Standard-B", "MALE"),
            Voice("en-AU", "en-AU-Standard-C", "FEMALE"),
            Voice("en-AU", "en-AU-Standard-D", "MALE"),
            Voice("en-IN", "en-IN-Standard-A", "FEMALE"),
            Voice("en-IN", "en-IN-Standard-B", "MALE"),
            Voice("en-IN", "en-IN-Standard-C", "MALE"),
            Voice("en-IN", "en-IN-Standard-D", "FEMALE"),
            Voice("en-GB", "en-GB-Neural2-A", "FEMALE"),
            Voice("en-GB", "en-GB-Neural2-B", "MALE"),
            Voice("en-GB", "en-GB-Neural2-C", "FEMALE"),
            Voice("en-GB", "en-GB-Neural2-D", "MALE"),
            Voice("en-GB", "en-GB-Neural2-F", "FEMALE"),
            Voice("en-GB", "en-GB-Standard-A", "FEMALE"),
            Voice("en-GB", "en-GB-Standard-B", "MALE"),
            Voice("en-GB", "en-GB-Standard-C", "FEMALE"),
            Voice("en-GB", "en-GB-Standard-D", "MALE"),
            Voice("en-GB", "en-GB-Standard-F", "FEMALE"),
        )
    }
}