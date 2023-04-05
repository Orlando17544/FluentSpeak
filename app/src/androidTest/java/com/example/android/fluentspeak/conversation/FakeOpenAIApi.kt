package com.example.android.fluentspeak.conversation

import com.example.android.fluentspeak.MESSAGE_ROLE
import com.example.android.fluentspeak.network.IOpenAIApi
import com.example.android.fluentspeak.network.*
import okhttp3.RequestBody

class FakeOpenAIApi: IOpenAIApi {
    override suspend fun getTranscriptionResponse(transcriptionParams: Map<String, RequestBody>): TranscriptionResponse {
        return TranscriptionResponse("whisper")
    }

    override suspend fun getTranslationResponse(translationParams: Map<String, RequestBody>): TranslationResponse {
        return TranslationResponse("whisper")
    }

    override suspend fun getChatCompletionResponse(chatCompletionRequestData: ChatCompletionRequestData): ChatCompletionResponse {
        return ChatCompletionResponse(
            "",
            "",
            0,
            listOf<Choice>(
                Choice(
                    0,
                    Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), "chatgpt"),
                    ""
                )
            ),
            Usage(0, 0, 0)
        )
    }
}