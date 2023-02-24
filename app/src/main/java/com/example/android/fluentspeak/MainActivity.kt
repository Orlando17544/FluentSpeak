package com.example.android.fluentspeak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.Toast
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}