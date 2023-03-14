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
    fun getWhisperResponse(whisperRequestData: WhisperRequestData): LiveData<WhisperResponse> {
        val result = MutableLiveData<WhisperResponse>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {


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

                try {
                    val whisperResponse: WhisperResponse =
                        OpenAIApi.retrofitService.getWhisperResponse(params)

                    result.postValue(whisperResponse)
                } catch (e: Exception) {
                    //result.postValue(WhisperResponse("Failure: ${e.message}"))
                    e.message?.let { Log.e("WHISPER_RESPONSE_ERROR", it) }
                }
            }
        }
        return result
    }

    fun getChatGPTResponse(chatGPTRequestData: ChatGPTRequestData): LiveData<ChatGPTResponse> {
        val result = MutableLiveData<ChatGPTResponse>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                try {
                    val chatGPTResponse: ChatGPTResponse = OpenAIApi.retrofitService.getChatGPTResponse(chatGPTRequestData)

                    result.postValue(chatGPTResponse)
                } catch (e: Exception) {
                    //result.postValue(Message("exception", "Failure: ${e.message}"))
                    e.message?.let { Log.e("CHATGPT_RESPONSE_ERROR", it) }
                }

            }
        }
        return result
    }

    fun getTextToSpeechResponse(): LiveData<TextToSpeechResponse> {
        val result = MutableLiveData<TextToSpeechResponse>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                try {

                    val textToSpeechRequestData = TextToSpeechRequestData(
                        Input("Hello, how are you?"),
                        Voice("en-gb", "en-GB-Standard-A", "FEMALE"),
                        AudioConfig("MP3")
                    )

                    val textToSpeechResponse: TextToSpeechResponse = GoogleCloudApi.retrofitService.getTextToSpeechResponse(textToSpeechRequestData)

                    result.postValue(textToSpeechResponse)
                } catch (e: Exception) {
                    //result.postValue(Message("exception", "Failure: ${e.message}"))
                    e.message?.let { Log.e("SPEECH_RESPONSE_ERROR", it) }
                }

            }
        }
        return result
    }

}
