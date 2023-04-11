package com.example.android.fluentspeak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.android.fluentspeak.databinding.ActivitySettingsBinding
import com.example.android.fluentspeak.network.Voice
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    lateinit var accentField: TextInputLayout
    lateinit var genderField: TextInputLayout
    lateinit var voiceNameField: TextInputLayout

    var voiceNameAccentFilter = ""
    var voiceNameGenderFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        binding.whisperTemperatureImage.setOnClickListener {
            Toast.makeText(this, "The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature until certain thresholds are hit.", Toast.LENGTH_LONG).show()
        }

        binding.chatGptTemperatureImage.setOnClickListener {
            Toast.makeText(this, "What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.\n" +
                    "\n" +
                    "We generally recommend altering this or top_p but not both.", Toast.LENGTH_LONG).show()
        }

        binding.chatGptMaxTokensImage.setOnClickListener {
            Toast.makeText(this, "The maximum number of tokens to generate in the chat completion.\n" +
                    "\n" +
                    "The total length of input tokens and generated tokens is limited by the model's context length.", Toast.LENGTH_LONG).show()
        }

        binding.chatGptPresencePenaltyImage.setOnClickListener {
            Toast.makeText(this, "Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.", Toast.LENGTH_LONG).show()
        }

        binding.chatGptFrecuencyPenaltyImage.setOnClickListener {
            Toast.makeText(this, "Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.", Toast.LENGTH_LONG).show()
        }

        binding.textToSpeechAccentImage.setOnClickListener {
            Toast.makeText(this, "AU means Australian\nIN means Indian\nGB means Great Britain", Toast.LENGTH_LONG).show()
        }

        binding.textToSpeechGenderImage.setOnClickListener {
            Toast.makeText(this, "Male voice or Female voice", Toast.LENGTH_LONG).show()
        }

        binding.textToSpeechVoiceNameImage.setOnClickListener {
            Toast.makeText(this, "The voice name that match with the accent and gender previously selected", Toast.LENGTH_LONG).show()
        }

        voiceNameField = binding.textToSpeechVoiceNameField
        accentField = binding.textToSpeechAccentField
        genderField = binding.textToSpeechGenderField

        val accentItems = TextToSpeechSettingsData.VOICES.map { it.languageCode }.distinct()
        val accentAdapter = ArrayAdapter(this, R.layout.item, accentItems)
        (accentField.editText as? AutoCompleteTextView)?.setAdapter(accentAdapter)

        val genderItems = TextToSpeechSettingsData.VOICES.map { it.ssmlGender }.distinct()
        val genderAdapter = ArrayAdapter(this, R.layout.item, genderItems)
        (genderField.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        updateVoiceNameField(TextToSpeechSettingsData.VOICES.map { it.name })

        (accentField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameAccentFilter = ((view as TextView).text as String)

            val newVoiceItems = filterVoices()

            updateVoiceNameField(newVoiceItems.map { it.name })
        }

        (genderField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            val newVoiceNameItems = filterVoices()

            updateVoiceNameField(newVoiceNameItems.map { it.name})
        }
    }

    fun updateVoiceNameField(voiceNameItems: List<String>) {
        val voiceNameAdapter = ArrayAdapter(this, R.layout.item, voiceNameItems)
        (voiceNameField.editText as? AutoCompleteTextView)?.setAdapter(voiceNameAdapter)
        (voiceNameField.editText as? AutoCompleteTextView)?.setText("")
    }

    fun filterVoices(): List<Voice> {
        return TextToSpeechSettingsData.VOICES.filter {
            if (voiceNameGenderFilter.equals("")) {
                it.languageCode.contains(voiceNameAccentFilter)
            } else {
                it.languageCode.contains(voiceNameAccentFilter) && it.ssmlGender.contains(Regex("^" + voiceNameGenderFilter + "$"))
            }
        }
    }
}