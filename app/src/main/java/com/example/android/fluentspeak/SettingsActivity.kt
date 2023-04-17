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
import com.google.android.material.snackbar.Snackbar
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

        sharedPref =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        getValuesFromSharedPreferences()

        voiceNameAccentFilter = accentField.editText?.text.toString()
        voiceNameGenderFilter = genderField.editText?.text.toString()

        setupHintListeners()

        setupAdapters()

        val newVoiceItems = filterVoices()

        updateVoiceNameField(newVoiceItems.map { it.name })

        setupFieldListeners()

        binding.save.setOnClickListener {
            if (binding.chatGptMaxTokensField.editText?.text.toString().equals("")
                && binding.textToSpeechVoiceNameField.editText?.text.toString().equals("")) {
                binding.chatGptMaxTokensField.error = getString(R.string.chat_gpt_max_tokens_error)
                binding.textToSpeechVoiceNameField.error = getString(R.string.text_to_speech_voice_name_error)
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else if (binding.chatGptMaxTokensField.editText?.text.toString().equals("")) {
                binding.chatGptMaxTokensField.error = getString(R.string.chat_gpt_max_tokens_error)
                binding.textToSpeechVoiceNameField.error = null
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else if (binding.textToSpeechVoiceNameField.editText?.text.toString().equals("")) {
                binding.textToSpeechVoiceNameField.error = getString(R.string.text_to_speech_voice_name_error)
                binding.chatGptMaxTokensField.error = null
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else {
                saveValuesToSharedPreferences()
                binding.chatGptMaxTokensField.error = null
                binding.textToSpeechVoiceNameField.error = null
                Snackbar.make(it, R.string.save_preferences_successful_message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun saveValuesToSharedPreferences() {
        with(sharedPref.edit()) {
            putFloat(
                getString(R.string.whisper_temperature_key),
                binding.whisperTemperatureSlider.value
            )

            putFloat(
                getString(R.string.chat_gpt_temperature_key),
                binding.chatGptTemperatureSlider.value
            )
            putInt(
                getString(R.string.chat_gpt_max_tokens_key),
                binding.chatGptMaxTokensField.editText?.text.toString().toInt()
            )
            putFloat(
                getString(R.string.chat_gpt_presence_penalty_key),
                binding.chatGptPresencePenaltySlider.value
            )
            putFloat(
                getString(R.string.chat_gpt_frecuency_penalty_key),
                binding.chatGptFrecuencyPenaltySlider.value
            )

            putString(
                getString(R.string.text_to_speech_accent_key),
                binding.textToSpeechAccentField.editText?.text.toString()
            )
            putString(
                getString(R.string.text_to_speech_gender_key),
                binding.textToSpeechGenderField.editText?.text.toString()
            )
            putString(
                getString(R.string.text_to_speech_voice_name_key),
                binding.textToSpeechVoiceNameField.editText?.text.toString()
            )
            apply()
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

    fun getValuesFromSharedPreferences() {
        binding.whisperTemperatureSlider.value =
            sharedPref.getFloat(getString(R.string.whisper_temperature_key), 0.0f)

        binding.chatGptTemperatureSlider.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_temperature_key), 0.0f)
        binding.chatGptMaxTokensField.editText?.setText(
            sharedPref.getInt(
                getString(R.string.chat_gpt_max_tokens_key),
                0
            ).toString()
        )
        binding.chatGptPresencePenaltySlider.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_presence_penalty_key), 0.0f)
        binding.chatGptFrecuencyPenaltySlider.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f)

        binding.textToSpeechAccentField.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_accent_key),
                ""
            )
        )
        binding.textToSpeechGenderField.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_gender_key),
                ""
            )
        )
        binding.textToSpeechVoiceNameField.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_voice_name_key),
                ""
            )
        )
    }

    fun setupHintListeners() {
        binding.whisperTemperatureImage.setOnClickListener {
            Toast.makeText(this, getString(R.string.whisper_temperature_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding.chatGptTemperatureImage.setOnClickListener {
            Toast.makeText(this, getString(R.string.chat_gpt_temperature_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding.chatGptMaxTokensImage.setOnClickListener {
            Toast.makeText(this, getString(R.string.chat_gpt_max_tokens_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding.chatGptPresencePenaltyImage.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.chat_gpt_presence_penalty_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding.chatGptFrecuencyPenaltyImage.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.chat_gpt_frecuency_penalty_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding.textToSpeechAccentImage.setOnClickListener {
            Toast.makeText(this, getString(R.string.text_to_speech_accent_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding.textToSpeechGenderImage.setOnClickListener {
            Toast.makeText(this, getString(R.string.text_to_speech_gender_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding.textToSpeechVoiceNameImage.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.text_to_speech_voice_name_hint),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setupAdapters() {
        val accentItems = TextToSpeechSettingsData.VOICES.map { it.languageCode }.distinct()
        val accentAdapter = ArrayAdapter(this, R.layout.item, accentItems)
        (accentField.editText as? AutoCompleteTextView)?.setAdapter(accentAdapter)

        val genderItems = TextToSpeechSettingsData.VOICES.map { it.ssmlGender }.distinct()
        val genderAdapter = ArrayAdapter(this, R.layout.item, genderItems)
        (genderField.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)
    }

    fun setupFieldListeners() {
        (accentField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameAccentFilter = ((view as TextView).text as String)

            val newVoiceItems = filterVoices()

            updateVoiceNameField(newVoiceItems.map { it.name })
            cleanVoiceNameField()
        }

        (genderField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            val newVoiceNameItems = filterVoices()

            updateVoiceNameField(newVoiceNameItems.map { it.name })
            cleanVoiceNameField()
        }
    }
}
