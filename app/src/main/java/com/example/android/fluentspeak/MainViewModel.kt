package com.example.android.fluentspeak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.fluentspeak.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
/*
    fun getChatBotResponse(): LiveData<String> {
        val result = MutableLiveData<String>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ChatBotApi.retrofitService.getResponse().enqueue(
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
    }*/
}