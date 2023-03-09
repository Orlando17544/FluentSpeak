package com.example.android.fluentspeak

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.*

class ConversationFragment : Fragment() {

    private lateinit var viewModel: ConversationViewModel

    lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_conversation, container, false)

        viewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)


        textToSpeech = TextToSpeech(this.context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US)
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        val speak: Button = view.findViewById(R.id.speak)

        speak.setOnClickListener {
            textToSpeech.speak("Hello, how are you?", TextToSpeech.QUEUE_FLUSH, null)
        }

        val chatbot: Button = view.findViewById(R.id.chatbot)
        val chatBotResponse: TextView = view.findViewById(R.id.chatbot_response)

        chatbot.setOnClickListener {
            viewModel.getChatBotResponse().observe(viewLifecycleOwner, { response ->
                chatBotResponse.text = response
            })
        }

        val whisper: Button = view.findViewById(R.id.whisper)
        val whisperResponse: TextView = view.findViewById(R.id.whisper_response)

        whisper.setOnClickListener {

        }

        return view
    }
}