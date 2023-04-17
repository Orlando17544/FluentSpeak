package com.example.android.fluentspeak

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.example.android.fluentspeak.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ConversationViewModel(private val apisRepository: ApisRepository) : ViewModel() {
    var unfinishedUserMessage: Message = Message(MESSAGE_ROLE.USER.toString().lowercase(), "")

    fun addMessageToUnfinishedUserMessage(userMessagePortion: Message) {
        unfinishedUserMessage.content += " " + userMessagePortion.content
        unfinishedUserMessage.content = unfinishedUserMessage.content.trim()
    }

    fun addMessageToConversationData(message: Message) {
        ConversationData.addMessage(message)
    }

    suspend fun getTranscriptionResponse(transcriptionRequestData: TranscriptionRequestData): TranscriptionResponse {
        val filePart: RequestBody = RequestBody.create(
            "audio/aac".toMediaTypeOrNull(),
            transcriptionRequestData.file
        )

        val modelPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            transcriptionRequestData.model
        )

        val promptPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            transcriptionRequestData.prompt
        )

        val responseFormatPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            transcriptionRequestData.responseFormat
        )

        val temperaturePart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            transcriptionRequestData.temperature.toString()
        )

        val languagePart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            transcriptionRequestData.language
        )

        val params: MutableMap<String, RequestBody> = mutableMapOf<String, RequestBody>()

        params.put("file\"; filename=\"recording.m4a\"", filePart)
        params.put("model", modelPart)
        params.put("prompt", promptPart)
        params.put("response_format", responseFormatPart)
        params.put("temperature", temperaturePart)
        params.put("language", languagePart)

        var transcriptionResponse: TranscriptionResponse? = null

        try {
            transcriptionResponse = apisRepository.openAIApi.getTranscriptionResponse(params)
        } catch (e: Exception) {
            e.message?.let { Log.e("TRANSCRI_RESPONSE_ERROR", it) }
        }

        return transcriptionResponse ?: TranscriptionResponse("")
    }

    suspend fun getTranslationResponse(translationRequestData: TranslationRequestData): TranslationResponse {
        val filePart: RequestBody = RequestBody.create(
            "audio/aac".toMediaTypeOrNull(),
            translationRequestData.file
        )

        val modelPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            translationRequestData.model
        )

        val promptPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            translationRequestData.prompt
        )

        val responseFormatPart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            translationRequestData.responseFormat
        )

        val temperaturePart: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            translationRequestData.temperature.toString()
        )

        val params: MutableMap<String, RequestBody> = mutableMapOf<String, RequestBody>()

        params.put("file\"; filename=\"recording.m4a\"", filePart)
        params.put("model", modelPart)
        params.put("prompt", promptPart)
        params.put("response_format", responseFormatPart)
        params.put("temperature", temperaturePart)

        var translationResponse: TranslationResponse? = null

        try {
            translationResponse = apisRepository.openAIApi.getTranslationResponse(params)
        } catch (e: Exception) {
            e.message?.let { Log.e("TRANSLAT_RESPONSE_ERROR", it) }
        }

        return translationResponse ?: TranslationResponse("")
    }

    suspend fun getChatCompletionResponse(chatCompletionRequestData: ChatCompletionRequestData): ChatCompletionResponse {
        var chatCompletionResponse: ChatCompletionResponse? = null

        try {
            chatCompletionResponse = apisRepository.openAIApi.getChatCompletionResponse(chatCompletionRequestData)
        } catch (e: Exception) {
            e.message?.let { Log.e("CHATCOMP_RESPONSE_ERROR", it) }
        }

        return chatCompletionResponse ?: ChatCompletionResponse("", "", 0, listOf<Choice>(), Usage(0, 0, 0))
    }

    suspend fun getTextToSpeechResponse(textToSpeechRequestData: TextToSpeechRequestData): TextToSpeechResponse {
        var textToSpeechResponse: TextToSpeechResponse? = null

        try {
            textToSpeechResponse =
                apisRepository.googleCloudApi.getTextToSpeechResponse(textToSpeechRequestData)
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