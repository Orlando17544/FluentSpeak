package com.example.android.fluentspeak.conversation

import android.util.Log
import com.example.android.fluentspeak.MESSAGE_ROLE
import com.example.android.fluentspeak.network.IOpenAIApi
import com.example.android.fluentspeak.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class FakeOpenAIApi: IOpenAIApi {
    override suspend fun getTranscriptionResponse(transcriptionParams: Map<String, RequestBody>): TranscriptionResponse {
        val promptPart = transcriptionParams.get("prompt")

        if (promptPart?.contentLength() == RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                ""
            ).contentLength())
        {
            return TranscriptionResponse("whisper1")
        } else if (promptPart?.contentLength() == RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                "whisper1"
            ).contentLength()) {
            return TranscriptionResponse("whisper10")
        } else {
            return TranscriptionResponse("whisper100")
        }
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
                    Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), "A: " + chatCompletionRequestData.messages[1].content),
                    ""
                )
            ),
            Usage(0, 0, 0)
        )
    }
}