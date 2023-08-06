package com.example.android.fluentspeak

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.android.fluentspeak.databinding.FragmentSettingsBinding
import com.example.android.fluentspeak.databinding.FragmentTopicsBinding
import com.example.android.fluentspeak.network.Voice
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class SettingsFragment : Fragment() {

    var binding: FragmentSettingsBinding? = null

    var accentField: TextInputLayout? = null
    var genderField: TextInputLayout? = null
    var voiceNameField: TextInputLayout? = null

    var voiceNameAccentFilter = ""
    var voiceNameGenderFilter = ""

    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings,
            container,
            false
        )

        voiceNameField = binding?.textToSpeechVoiceNameField
        accentField = binding?.textToSpeechAccentField
        genderField = binding?.textToSpeechGenderField

        sharedPref =
            requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        getValuesFromSharedPreferences()

        voiceNameAccentFilter = accentField?.editText?.text.toString()
        voiceNameGenderFilter = genderField?.editText?.text.toString()

        setupHintListeners()

        setupAdapters()

        val newVoiceItems = filterVoices()

        updateVoiceNameField(newVoiceItems.map { it.name })

        setupFieldListeners()

        binding?.saveButton?.setOnClickListener {
            if (binding?.chatGptMaxTokensField?.editText?.text.toString().equals("")
                && binding?.textToSpeechVoiceNameField?.editText?.text.toString().equals("")) {
                binding?.chatGptMaxTokensField?.error = getString(R.string.chat_gpt_max_tokens_error)
                binding?.textToSpeechVoiceNameField?.error = getString(R.string.text_to_speech_voice_name_error)
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else if (binding?.chatGptMaxTokensField?.editText?.text.toString().equals("")) {
                binding?.chatGptMaxTokensField?.error = getString(R.string.chat_gpt_max_tokens_error)
                binding?.textToSpeechVoiceNameField?.error = null
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else if (binding?.textToSpeechVoiceNameField?.editText?.text.toString().equals("")) {
                binding?.textToSpeechVoiceNameField?.error = getString(R.string.text_to_speech_voice_name_error)
                binding?.chatGptMaxTokensField?.error = null
                Snackbar.make(it, R.string.save_preferences_failed_message, Snackbar.LENGTH_SHORT).show()
            } else if (binding?.usernameField?.editText?.text.toString().equals("")) {
                binding?.usernameField?.error = getString(R.string.username_error)
            } else if (binding?.utterancesAtBeginningField?.editText?.text.toString().equals("")) {
                binding?.utterancesAtBeginningField?.error = getString(R.string.utterances_at_beginning_error)
            } else if (binding?.lastMessagesField?.editText?.text.toString().equals("")) {
                binding?.lastMessagesField?.error = getString(R.string.last_messages_error)
            } else {
                saveValuesToSharedPreferences()
                binding?.chatGptMaxTokensField?.error = null
                binding?.textToSpeechVoiceNameField?.error = null
                binding?.usernameField?.error = null
                binding?.utterancesAtBeginningField?.error = null
                binding?.lastMessagesField?.error = null
                Snackbar.make(it, R.string.save_preferences_successful_message, Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding?.root
    }

    fun saveValuesToSharedPreferences() {
        with(sharedPref.edit()) {
            binding?.whisperTemperatureSlider?.value?.let {
                putFloat(
                    getString(com.example.android.fluentspeak.R.string.whisper_temperature_key),
                    it
                )
            }

            binding?.chatGptTemperatureSlider?.value?.let {
                putFloat(
                    getString(com.example.android.fluentspeak.R.string.chat_gpt_temperature_key),
                    it
                )
            }
            putInt(
                getString(com.example.android.fluentspeak.R.string.chat_gpt_max_tokens_key),
                binding?.chatGptMaxTokensField?.editText?.text.toString().toInt()
            )
            binding?.chatGptPresencePenaltySlider?.value?.let {
                putFloat(
                    getString(com.example.android.fluentspeak.R.string.chat_gpt_presence_penalty_key),
                    it
                )
            }
            binding?.chatGptFrecuencyPenaltySlider?.value?.let {
                putFloat(
                    getString(com.example.android.fluentspeak.R.string.chat_gpt_frecuency_penalty_key),
                    it
                )
            }

            putString(
                getString(com.example.android.fluentspeak.R.string.text_to_speech_accent_key),
                binding?.textToSpeechAccentField?.editText?.text.toString()
            )
            putString(
                getString(com.example.android.fluentspeak.R.string.text_to_speech_gender_key),
                binding?.textToSpeechGenderField?.editText?.text.toString()
            )
            putString(
                getString(com.example.android.fluentspeak.R.string.text_to_speech_voice_name_key),
                binding?.textToSpeechVoiceNameField?.editText?.text.toString()
            )

            val username = binding?.usernameField?.editText?.text.toString()

            putString(
                getString(com.example.android.fluentspeak.R.string.username_key),
                username[0].uppercase() + username.subSequence(1, username.length).toString().lowercase()
            )
            putInt(
                getString(com.example.android.fluentspeak.R.string.utterances_at_beginning_key),
                binding?.utterancesAtBeginningField?.editText?.text.toString().toInt()
            )
            putInt(
                getString(com.example.android.fluentspeak.R.string.last_messages_key),
                binding?.lastMessagesField?.editText?.text.toString().toInt()
            )
            apply()
        }
    }

    fun updateVoiceNameField(voiceNameItems: List<String>) {
        val voiceNameAdapter = ArrayAdapter(requireContext(), R.layout.item, voiceNameItems)
        (voiceNameField?.editText as? AutoCompleteTextView)?.setAdapter(voiceNameAdapter)
    }

    fun cleanVoiceNameField() {
        (voiceNameField?.editText as? AutoCompleteTextView)?.setText("")
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
        binding?.whisperTemperatureSlider?.value =
            sharedPref.getFloat(getString(R.string.whisper_temperature_key), 0.0f)

        binding?.chatGptTemperatureSlider?.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_temperature_key), 0.0f)
        binding?.chatGptMaxTokensField?.editText?.setText(
            sharedPref.getInt(
                getString(R.string.chat_gpt_max_tokens_key),
                0
            ).toString()
        )
        binding?.chatGptPresencePenaltySlider?.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_presence_penalty_key), 0.0f)
        binding?.chatGptFrecuencyPenaltySlider?.value =
            sharedPref.getFloat(getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f)

        binding?.textToSpeechAccentField?.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_accent_key),
                ""
            )
        )
        binding?.textToSpeechGenderField?.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_gender_key),
                ""
            )
        )
        binding?.textToSpeechVoiceNameField?.editText?.setText(
            sharedPref.getString(
                getString(R.string.text_to_speech_voice_name_key),
                ""
            )
        )
        binding?.usernameField?.editText?.setText(
            sharedPref.getString(
                getString(R.string.username_key),
                ""
            )
        )
        binding?.utterancesAtBeginningField?.editText?.setText(
            sharedPref.getInt(
                getString(R.string.utterances_at_beginning_key),
                0
            ).toString()
        )

        binding?.lastMessagesField?.editText?.setText(
            sharedPref.getInt(
                getString(R.string.last_messages_key),
                0
            ).toString()
        )
    }

    fun setupHintListeners() {
        binding?.whisperTemperatureImage?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.whisper_temperature_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding?.chatGptTemperatureImage?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.chat_gpt_temperature_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding?.chatGptMaxTokensImage?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.chat_gpt_max_tokens_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding?.chatGptPresencePenaltyImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.chat_gpt_presence_penalty_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding?.chatGptFrecuencyPenaltyImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.chat_gpt_frecuency_penalty_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding?.textToSpeechAccentImage?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.text_to_speech_accent_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding?.textToSpeechGenderImage?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.text_to_speech_gender_hint), Toast.LENGTH_LONG)
                .show()
        }

        binding?.textToSpeechVoiceNameImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.text_to_speech_voice_name_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding?.usernameImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.conversation_settings_username_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding?.utterancesAtBeginningImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.conversation_settings_utterances_at_beginning_hint),
                Toast.LENGTH_LONG
            ).show()
        }

        binding?.lastMessagesImage?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.conversation_settings_last_messages_hint),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setupAdapters() {
        val accentItems = TextToSpeechSettingsData.VOICES.map { it.languageCode }.distinct()
        val accentAdapter = ArrayAdapter(requireContext(), R.layout.item, accentItems)
        (accentField?.editText as? AutoCompleteTextView)?.setAdapter(accentAdapter)

        val genderItems = TextToSpeechSettingsData.VOICES.map { it.ssmlGender }.distinct()
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.item, genderItems)
        (genderField?.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)
    }

    fun setupFieldListeners() {
        (accentField?.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameAccentFilter = ((view as TextView).text as String)

            val newVoiceItems = filterVoices()

            updateVoiceNameField(newVoiceItems.map { it.name })
            cleanVoiceNameField()
        }

        (genderField?.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            val newVoiceNameItems = filterVoices()

            updateVoiceNameField(newVoiceNameItems.map { it.name })
            cleanVoiceNameField()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null

        accentField = null
        genderField = null
        voiceNameField = null
    }
}