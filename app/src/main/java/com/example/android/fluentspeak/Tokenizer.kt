package com.example.android.fluentspeak

import com.example.android.fluentspeak.network.Message

class Tokenizer {
    enum class TYPE {
        INPUT, OUTPUT
    }
    companion object {
        var inputTokens = 0
        var outputTokens = 0
        val ONE_USD = 17.08
        val ONE_THOUSAND_INPUT_TOKENS = 0.0015
        val ONE_THOUSAND_OUTPUT_TOKENS = 0.0020

        fun countTokens(messages: List<Message>, type: TYPE) {
            val builder = StringBuilder()
            for (message in messages) {
                builder.append(message.content)
                builder.append(" ")
            }

            val text = builder.toString()

            lateinit var formattedText: String

            formattedText = text.replace("(\t|\r|\n)".toRegex(), " ")

            formattedText = formattedText.replace(" {2,}".toRegex(), " ")

            val words = formattedText.split(" ").size
            // 100 tokens = 75 words
            val tokens = words * 100 / 75

            if (type.equals(TYPE.INPUT)) {
                inputTokens += tokens
            } else if (type.equals(TYPE.OUTPUT)) {
                outputTokens += tokens
            }
        }

        fun getCostMXN(): Double {
            val inputCostMXN = inputTokens * ONE_THOUSAND_INPUT_TOKENS * ONE_USD / 1000
            val outputCostMXN = outputTokens * ONE_THOUSAND_OUTPUT_TOKENS * ONE_USD / 1000

            return inputCostMXN + outputCostMXN
        }
    }
}