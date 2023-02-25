package com.example.android.fluentspeak

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.fluentspeak.network.ChatBotApi
import com.example.android.fluentspeak.network.ChatBotApiService
import com.example.android.fluentspeak.network.ChatBotResponse
import com.example.android.fluentspeak.network.RequestData
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class MainViewModel : ViewModel() {

    fun getChatBotResponse(): LiveData<String> {
        val result = MutableLiveData<String>();

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ChatBotApi.retrofitService.getResponse(RequestData(false, false, "Hello, how are you?")).enqueue(
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