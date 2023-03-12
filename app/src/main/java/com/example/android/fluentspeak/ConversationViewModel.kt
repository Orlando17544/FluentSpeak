package com.example.android.fluentspeak

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class ConversationViewModel: ViewModel() {
    fun getWhisperResponse(filePath: String): LiveData<WhisperResponse> {
        val result = MutableLiveData<WhisperResponse>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(filePath)

                val filePart: RequestBody = RequestBody.create(
                    "audio/aac".toMediaTypeOrNull(),
                    file
                )

                val modelPart: RequestBody = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    "whisper-1"
                )

                try {
                    val whisperResponse: WhisperResponse = Api.retrofitService.getWhisperResponse(filePart, modelPart)

                    result.postValue(whisperResponse)
                } catch (e: Exception) {
                    result.postValue(WhisperResponse("Failure: ${e.message}"))
                }
            }
        }
        return result
    }

    fun getChatGPTResponse(): LiveData<Message> {
        val result = MutableLiveData<Message>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val messages = mutableListOf(
                    Message("system", "You are a helpful assistant."),
                    Message("user", "Who won the world series in 2020?")
                )

                try {
                    val chatGPTResponse: ChatGPTResponse = Api.retrofitService.getChatGPTResponse(ChatGPTRequestData("gpt-3.5-turbo", messages))

                    result.postValue(chatGPTResponse.message)
                } catch (e: Exception) {
                    result.postValue(Message("exception","Failure: ${e.message}"))
                }

                /*
                WhisperApi.retrofitService.getResponse(WhisperRequestData("30414ee7c4fffc37e260fcab7842b5be470b9b840f2b608f5baa9bbef9a259ed", false, "Hello, how are you?")).enqueue(
                    object: Callback<ChatBotResponse> {
                        override fun onResponse(
                            call: Call<ChatBotResponse>,
                            response: Response<ChatBotResponse>
                        ) {
                            result.value = response.body()?.message
                        }

                        override fun onFailure(call: Call<ChatBotResponse>, t: Throwable) {
                            result.value = t.message
                        }

                    }
                )*/
            }
        }
        return result
    }
}
