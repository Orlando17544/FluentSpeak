package com.example.android.fluentspeak

import android.util.Log
import androidx.lifecycle.*
import com.example.android.fluentspeak.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


class ConversationViewModel(private val apisRepository: ApisRepository) : ViewModel() {
    var unfinishedUserMessage: Message = Message(MESSAGE_ROLE.USER, "")

    fun addMessageToUnfinishedUserMessage(userMessagePortion: Message) {
        unfinishedUserMessage.content += " " + userMessagePortion.content
        unfinishedUserMessage.content = unfinishedUserMessage.content.trim()
    }

    fun addMessageToConversationData(message: Message) {
        ConversationData.addMessage(message)
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
            whisperResponse = apisRepository.openAIApi.getWhisperResponse(params)
            //whisperResponse = ApisRepository.openAIApi.retrofitService.getWhisperResponse(params)
            //whisperResponse = OpenAIApi.retrofitService.getWhisperResponse(params)
        } catch (e: Exception) {
            e.message?.let { Log.e("WHISPER_RESPONSE_ERROR", it) }
        }

        println(whisperResponse?.text)

        return whisperResponse ?: WhisperResponse("")
    }

    suspend fun getChatGPTResponse(chatGPTRequestData: ChatGPTRequestData): ChatGPTResponse {
        var chatGPTResponse: ChatGPTResponse? = null

        try {
            chatGPTResponse = apisRepository.openAIApi.getChatGPTResponse(chatGPTRequestData)
            //chatGPTResponse = ApisRepository.openAIApi.retrofitService.getChatGPTResponse(chatGPTRequestData)
            //chatGPTResponse = OpenAIApi.retrofitService.getChatGPTResponse(chatGPTRequestData)
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
                apisRepository.googleCloudApi.getTextToSpeechResponse(textToSpeechRequestData)
                //GoogleCloudApi.retrofitService.getTextToSpeechResponse(textToSpeechRequestData)
        } catch (e: Exception) {
            //result.postValue(Message("exception", "Failure: ${e.message}"))
            e.message?.let { Log.e("SPEECH_RESPONSE_ERROR", it) }
        }

        return textToSpeechResponse ?: TextToSpeechResponse("")
    }
}

@Suppress("UNCHECKED_CAST")
class ConversationViewModelFactory (
    private val apisRepository: ApisRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (ConversationViewModel(apisRepository) as T)
}