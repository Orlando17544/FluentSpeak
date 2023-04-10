package com.example.android.fluentspeak

data class Accent(
    val name: String, val value: String
)

data class Gender(
    val name: String, val value: String
)

data class VoiceName(
    val name: String, val value: String
)

class TextToSpeechSettingsData {
    companion object {
        val ACCENTS = listOf<Accent>(
            Accent("AU", "Australian"),
            Accent("IN", "Indian"),
            Accent("GB", "Great Britain")
        )

        val GENDERS = listOf<Gender>(
            Gender("MALE", "Male"),
            Gender("FEMALE", "Female")
        )

        val VOICE_NAMES = listOf<VoiceName>(
            VoiceName("en-AU-Neural2-A", "en-AU-Neural2-A-F"),
            VoiceName("en-AU-Neural2-B", "en-AU-Neural2-B-M"),
            VoiceName("en-AU-Neural2-C", "en-AU-Neural2-C-F"),
            VoiceName("en-AU-Neural2-D", "en-AU-Neural2-D-M"),
            VoiceName("en-AU-Standard-A", "en-AU-Standard-A-F"),
            VoiceName("en-AU-Standard-B", "en-AU-Standard-B-M"),
            VoiceName("en-AU-Standard-C", "en-AU-Standard-C-F"),
            VoiceName("en-AU-Standard-D", "en-AU-Standard-D-M"),
            VoiceName("en-IN-Standard-A", "en-IN-Standard-A-F"),
            VoiceName("en-IN-Standard-B", "en-IN-Standard-B-M"),
            VoiceName("en-IN-Standard-C", "en-IN-Standard-C-M"),
            VoiceName("en-IN-Standard-D", "en-IN-Standard-D-F"),
            VoiceName("en-GB-Neural2-A", "en-GB-Neural2-A-F"),
            VoiceName("en-GB-Neural2-B", "en-GB-Neural2-B-M"),
            VoiceName("en-GB-Neural2-C", "en-GB-Neural2-C-F"),
            VoiceName("en-GB-Neural2-D", "en-GB-Neural2-D-M"),
            VoiceName("en-GB-Neural2-F", "en-GB-Neural2-F-F"),
            VoiceName("en-GB-Standard-A", "en-GB-Standard-A-F"),
            VoiceName("en-GB-Standard-B", "en-GB-Standard-B-M"),
            VoiceName("en-GB-Standard-C", "en-GB-Standard-C-F"),
            VoiceName("en-GB-Standard-D", "en-GB-Standard-D-M"),
            VoiceName("en-GB-Standard-F", "en-GB-Standard-F-F"),
        )
    }
}