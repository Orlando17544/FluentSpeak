package com.example.android.fluentspeak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US)
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        val speak: Button = findViewById(R.id.speak)

        speak.setOnClickListener {
            textToSpeech.speak("Hello, how are you?", TextToSpeech.QUEUE_FLUSH, null)
        }

        val chatbot: Button = findViewById(R.id.chatbot)
        val chatBotResponse: TextView = findViewById(R.id.chatbot_response)

        chatbot.setOnClickListener {
            viewModel.getChatBotResponse().observe(this, { response ->
                chatBotResponse.text = response
            })
        }
    }
}