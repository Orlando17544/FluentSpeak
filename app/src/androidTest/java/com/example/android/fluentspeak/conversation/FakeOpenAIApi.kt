package com.example.android.fluentspeak.conversation

import com.example.android.fluentspeak.MESSAGE_ROLE
import com.example.android.fluentspeak.network.IOpenAIApi
import com.example.android.fluentspeak.network.*
import okhttp3.RequestBody

class FakeOpenAIApi: IOpenAIApi {
    override suspend fun getWhisperResponse(whisperParams: Map<String, RequestBody>): WhisperResponse {
        return WhisperResponse("whisper")
    }

    override suspend fun getChatGPTResponse(chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse {
        return ChatGPTResponse(
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