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
    fun getChatBotResponse(filePath: String): LiveData<String> {
        val result = MutableLiveData<String>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(filePath)

                val filePart: RequestBody = RequestBody.create(
                    null,
                    file
                )

                val modelPart: RequestBody = RequestBody.create(
                    null,
                    "whisper-1"
                )

                /*val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(
                    "audio/mpeg", file));

                val textPart: MultipartBody.Part = MultipartBody.Part.createFormData("text", "model", RequestBody.create("text/plain", "whisper-1"))*/

                ChatBotApi.retrofitService.getResponse(filePart, modelPart)?.enqueue(
                    object: Callback<ChatBotResponse?> {
                        override fun onResponse(
                            call: Call<ChatBotResponse?>,
                            response: Response<ChatBotResponse?>
                        ) {
                            result.value = response.body()?.message
                        }

                        override fun onFailure(call: Call<ChatBotResponse?>, t: Throwable) {
                            result.value = t.message
                        }
                    }
                )
            }
        }
        return result
    }

    fun getWhisperResponse(): LiveData<String> {
        val result = MutableLiveData<String>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
                )
            }
        }
        return result
    }
}
