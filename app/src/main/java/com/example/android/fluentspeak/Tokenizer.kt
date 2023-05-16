package com.example.android.fluentspeak

class Tokenizer {
    companion object {
        var totalTokens = 0

        fun countTokens(text: String) {

            lateinit var formattedText: String

            formattedText = text.replace("(\t|\r|\n)".toRegex(), " ")

            formattedText = formattedText.replace(" {2,}".toRegex(), " ")

            val words = formattedText.split(" ").size
            // 100 tokens = 75 words
            val tokens = words * 100 / 75

            totalTokens += tokens
        }
    }
}