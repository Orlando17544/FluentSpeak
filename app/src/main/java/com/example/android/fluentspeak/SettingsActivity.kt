package com.example.android.fluentspeak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
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

            when(voiceNameAccentFilter) {
                accentItems[0] -> voiceNameAccentFilter = accentItems[0]
                accentItems[1] -> voiceNameAccentFilter = accentItems[1]
                accentItems[2] -> voiceNameAccentFilter = accentItems[2]
            }

            val newVoiceItems = filterVoices()

            updateVoiceNameField(newVoiceItems.map { it.name })
        }

        (genderField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            when(voiceNameGenderFilter) {
                genderItems[0] -> voiceNameGenderFilter = genderItems[0]
                genderItems[1] -> voiceNameGenderFilter = genderItems[1]
                genderItems[2] -> voiceNameGenderFilter = genderItems[2]
            }

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