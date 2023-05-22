package com.example.android.fluentspeak

import android.util.Log
import androidx.lifecycle.*
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.util.concurrent.TimeoutException

class ChatViewModel(private val apisRepository: ApisRepository) : ViewModel() {
    private val _systemMessage = Message(MESSAGE_ROLE.SYSTEM.toString().lowercase(), "You are a dialogue creator")
    val systemMessage: Message
        get() = _systemMessage

    private val _unfinishedMessage = Message(MESSAGE_ROLE.USER.toString().lowercase(), "")
    val unfinishedMessage: Message
        get() = _unfinishedMessage

    private val _messages: MutableList<Message> = mutableListOf()
    val messages: List<Message>
        get() = _messages

    private val _conversations = MutableLiveData<List<ConversationWithUtterances>>()
    val conversations: LiveData<List<ConversationWithUtterances>> = _conversations

    private val _currentConversation = MutableLiveData<Int>(0)
    val currentConversation: LiveData<Int> = _currentConversation

    private val _previousConversation = MutableLiveData<Int>(0)
    val previousConversation: LiveData<Int> = _previousConversation

    fun setConversations(conversations: List<ConversationWithUtterances>) {
        _conversations.value = conversations
    }

    fun nextConversation() {
        _currentConversation.value = _currentConversation.value?.plus(1)
    }

    fun updatePreviousConversation() {
        _previousConversation.value = _currentConversation.value
    }

    fun addMessageToUnfinishedMessage(messagePortion: Message) {
        _unfinishedMessage.content += " " + messagePortion.content
        _unfinishedMessage.content = _unfinishedMessage.content.trim()
    }

    fun cleanUnfinishedMessage() {
        _unfinishedMessage.content = ""
    }

    fun addMessage(message: Message) {
        if (message.content == "") {
            return
        }
        _messages.add(message)
    }

    fun cleanMessages() {
        _messages.clear()
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

        textToSpeechResponse =
            apisRepository.googleCloudApi.getTextToSpeechResponse(textToSpeechRequestData)
        /*try {

        } catch (e: TimeoutException) {
            //result.postValue(Message("exception", "Failure: ${e.message}"))
            e.message?.let { Log.e("SPEECH_RESPONSE_ERROR", it) }
        }*/



        return textToSpeechResponse ?: TextToSpeechResponse("")
    }
}

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory (
    private val apisRepository: ApisRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (ChatViewModel(apisRepository) as T)
}