package com.example.android.fluentspeak

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.fluentspeak.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.w3c.dom.Text


class ConversationViewModel : ViewModel() {
    var unfinishedUserMessage: Message = Message(MESSAGE_ROLE.USER, "")

    fun addMessageToUnfinishedUserMessage(userMessagePortion: Message) {
        unfinishedUserMessage.content += " " + userMessagePortion.content
        unfinishedUserMessage.content = unfinishedUserMessage.content.trim()
    }

    suspend fun getWhisperResponse(whisperRequestData: WhisperRequestData): WhisperResponse {
        val filePart: RequestBody = RequestBody.create(
            "audio/aac".toMediaTypeOrNull(),
            whisperRequestData.file
        )

        val modelPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            whisperRequestData.model
        )

        val promptPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            whisperRequestData.prompt
        )

        val responseFormatPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            whisperRequestData.responseFormat
        )

        val temperaturePart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            whisperRequestData.temperature.toString()
        )

        val languagePart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            whisperRequestData.language
        )

        val params: MutableMap<String, RequestBody> = mutableMapOf<String, RequestBody>()

        params.put("file\"; filename=\"recording.m4a\"", filePart)
        params.put("model", modelPart)
        params.put("prompt", promptPart)
        params.put("response_format", responseFormatPart)
        params.put("temperature", temperaturePart)
        params.put("language", languagePart)

        var whisperResponse: WhisperResponse? = null

        try {
            whisperResponse = OpenAIApi.retrofitService.getWhisperResponse(params)
        } catch (e: Exception) {
            e.message?.let { Log.e("WHISPER_RESPONSE_ERROR", it) }
        }

        return whisperResponse ?: WhisperResponse("")
    }

    suspend fun getChatGPTResponse(chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse {
        var chatGPTResponse: ChatGPTResponse? = null

        try {
            chatGPTResponse = OpenAIApi.retrofitService.getChatGPTResponse(chatGPTRequestData)
        } catch (e: Exception) {
            e.message?.let { Log.e("CHATGPT_RESPONSE_ERROR", it) }
        }

        return chatGPTResponse ?: ChatGPTResponse("", "", 0, listOf<Choice>(), Usage(0, 0, 0))
    }

    suspend fun getTextToSpeechResponse(textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse {
        var textToSpeechResponse: TextToSpeechResponse? = null

        try {
            val textToSpeechRequestData = TextToSpeechRequestData(
                Input("Hello, how are you?"),
                Voice("en-gb", "en-GB-Standard-A", "FEMALE"),
                AudioConfig("MP3")
            )

            textToSpeechResponse =
                GoogleCloudApi.retrofitService.getTextToSpeechResponse(textToSpeechRequestData)
        } catch (e: Exception) {
            //result.postValue(Message("exception", "Failure: ${e.message}"))
            e.message?.let { Log.e("SPEECH_RESPONSE_ERROR", it) }
        }

        return textToSpeechResponse ?: TextToSpeechResponse("")
    }
}