package com.example.android.fluentspeak

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.android.fluentspeak.databinding.ActivitySettingsBinding
import com.example.android.fluentspeak.network.Voice
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    lateinit var accentField: TextInputLayout
    lateinit var genderField: TextInputLayout
    lateinit var voiceNameField: TextInputLayout

    var voiceNameAccentFilter = ""
    var voiceNameGenderFilter = ""

    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        voiceNameField = binding.textToSpeechVoiceNameField
        accentField = binding.textToSpeechAccentField
        genderField = binding.textToSpeechGenderField

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        getValueFromSharedPreferences(resources.getFraction(R.fraction.whisper_temperature_default_value, 1, 1), getString(R.string.whisper_temperature_key), binding.whisperTemperatureSlider)

        getValueFromSharedPreferences(resources.getFraction(R.fraction.chat_gpt_temperature_default_value, 1, 1), getString(R.string.chat_gpt_temperature_key), binding.chatGptTemperatureSlider)
        getValueFromSharedPreferences(resources.getInteger(R.integer.chat_gpt_max_tokens_default_value), getString(R.string.chat_gpt_max_tokens_key), binding.chatGptMaxTokensField.editText)
        getValueFromSharedPreferences(resources.getFraction(R.fraction.chat_gpt_presence_penalty_default_value, 1, 1), getString(R.string.chat_gpt_presence_penalty_key), binding.chatGptPresencePenaltySlider)

        getValueFromSharedPreferences(resources.getFraction(R.fraction.chat_gpt_frecuency_penalty_default_value, 1, 1), getString(R.string.chat_gpt_frecuency_penalty_key), binding.chatGptFrecuencyPenaltySlider)

        getValueFromSharedPreferences(resources.getString(R.string.text_to_speech_accent_default_value), getString(R.string.text_to_speech_accent_key), binding.textToSpeechAccentField.editText)
        getValueFromSharedPreferences(resources.getString(R.string.text_to_speech_gender_default_value), getString(R.string.text_to_speech_gender_key), binding.textToSpeechGenderField.editText)
        getValueFromSharedPreferences(resources.getString(R.string.text_to_speech_voice_name_default_value), getString(R.string.text_to_speech_voice_name_key), binding.textToSpeechVoiceNameField.editText)

        voiceNameAccentFilter = accentField.editText?.text.toString()
        voiceNameGenderFilter = genderField.editText?.text.toString()

        setupListeners()

        val accentItems = TextToSpeechSettingsData.VOICES.map { it.languageCode }.distinct()
        val accentAdapter = ArrayAdapter(this, R.layout.item, accentItems)
        (accentField.editText as? AutoCompleteTextView)?.setAdapter(accentAdapter)

        val genderItems = TextToSpeechSettingsData.VOICES.map { it.ssmlGender }.distinct()
        val genderAdapter = ArrayAdapter(this, R.layout.item, genderItems)
        (genderField.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        val newVoiceItems = filterVoices()

        updateVoiceNameField(newVoiceItems.map { it.name })

        (accentField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameAccentFilter = ((view as TextView).text as String)

            val newVoiceItems = filterVoices()

            updateVoiceNameField(newVoiceItems.map { it.name })
            cleanVoiceNameField()
        }

        (genderField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            val newVoiceNameItems = filterVoices()

            updateVoiceNameField(newVoiceNameItems.map { it.name})
            cleanVoiceNameField()
        }

        binding.save.setOnClickListener {
            saveValueToSharedPreferences(getString(R.string.whisper_temperature_key), binding.whisperTemperatureSlider.value)

            saveValueToSharedPreferences(getString(R.string.chat_gpt_temperature_key), binding.chatGptTemperatureSlider.value)
            saveValueToSharedPreferences(getString(R.string.chat_gpt_max_tokens_key), binding.chatGptMaxTokensField.editText?.text.toString())
            saveValueToSharedPreferences(getString(R.string.chat_gpt_presence_penalty_key), binding.chatGptPresencePenaltySlider.value)
            saveValueToSharedPreferences(getString(R.string.chat_gpt_frecuency_penalty_key), binding.chatGptFrecuencyPenaltySlider.value)

            saveValueToSharedPreferences(getString(R.string.text_to_speech_accent_key), binding.textToSpeechAccentField.editText?.text.toString())
            saveValueToSharedPreferences(getString(R.string.text_to_speech_gender_key), binding.textToSpeechGenderField.editText?.text.toString())
            saveValueToSharedPreferences(getString(R.string.text_to_speech_voice_name_key), binding.textToSpeechVoiceNameField.editText?.text.toString())
        }
    }

    fun saveValueToSharedPreferences(key: String, value: Any) {
        if (value.equals("")) {
            return
        }

        if (value is Float) {
            with (sharedPref.edit()) {
                putFloat(key, value)
                apply()
            }
        } else if (value is Int) {
            with (sharedPref.edit()) {
                putInt(key, value)
                apply()
            }
        } else if (value is String) {
            with (sharedPref.edit()) {
                putString(key, value)
                apply()
            }
        }
    }

    fun updateVoiceNameField(voiceNameItems: List<String>) {
        val voiceNameAdapter = ArrayAdapter(this, R.layout.item, voiceNameItems)
        (voiceNameField.editText as? AutoCompleteTextView)?.setAdapter(voiceNameAdapter)
    }

    fun cleanVoiceNameField() {
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

    fun getValueFromSharedPreferences(defaultValue: Any, key: String, view: View?) {
        if (defaultValue is Float) {
            val savedValue = sharedPref.getFloat(key, defaultValue)
            (view as Slider).value = savedValue
        } else if (defaultValue is Int) {
            val savedValue = sharedPref.getInt(key, defaultValue)
            (view as EditText).setText(savedValue.toString())
        } else if (defaultValue is String) {
            val savedValue = sharedPref.getString(key, defaultValue)
            (view as EditText).setText(savedValue)
        }
    }

    fun setupListeners() {
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
    }
}
