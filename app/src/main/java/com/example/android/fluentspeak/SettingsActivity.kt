package com.example.android.fluentspeak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.android.fluentspeak.databinding.ActivitySettingsBinding
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    lateinit var accentField: TextInputLayout
    lateinit var genderField: TextInputLayout
    lateinit var voiceNameField: TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_settings)
        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)

        voiceNameField = binding.textToSpeechVoiceNameField
        accentField = binding.textToSpeechAccentField
        genderField = binding.textToSpeechGenderField

        var voiceNameAccentFilter = ""
        var voiceNameGenderFilter = ""

        val accentItems = TextToSpeechSettingsData.ACCENTS.map { it.value }
        val accentAdapter = ArrayAdapter(this, R.layout.item, accentItems)
        (accentField.editText as? AutoCompleteTextView)?.setAdapter(accentAdapter)

        val genderItems = TextToSpeechSettingsData.GENDERS.map { it.value }
        val genderAdapter = ArrayAdapter(this, R.layout.item, genderItems)
        (genderField.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        val voiceNameItems = TextToSpeechSettingsData.VOICE_NAMES.map { it.value }
        updateVoiceNameField(voiceNameItems)

        (accentField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameAccentFilter = ((view as TextView).text as String)

            val accents = TextToSpeechSettingsData.ACCENTS

            when(voiceNameAccentFilter) {
                accents[0].value -> voiceNameAccentFilter = accents[0].name
                accents[1].value -> voiceNameAccentFilter = accents[1].name
                accents[2].value -> voiceNameAccentFilter = accents[2].name
            }

            val newVoiceNameItems = voiceNameItems.filter {
                if (voiceNameGenderFilter.length > 0) {
                    it.contains(voiceNameAccentFilter) && it.endsWith(voiceNameGenderFilter[0])
                } else {
                    it.contains(voiceNameAccentFilter)
                }
            }

            updateVoiceNameField(newVoiceNameItems)
        }

        (genderField.editText as AutoCompleteTextView).setOnItemClickListener { adapterView, view, position, id ->
            voiceNameGenderFilter = ((view as TextView).text as String)

            val genders = TextToSpeechSettingsData.GENDERS

            when(voiceNameGenderFilter) {
                genders[0].value -> voiceNameGenderFilter = genders[0].name
                genders[1].value -> voiceNameGenderFilter = genders[1].name
                genders[2].value -> voiceNameGenderFilter = genders[2].name
            }

            val newVoiceNameItems = voiceNameItems.filter {
                it.endsWith(voiceNameGenderFilter[0]) && it.contains(voiceNameAccentFilter)
            }

            updateVoiceNameField(newVoiceNameItems)
        }
    }

    fun updateVoiceNameField(voiceNameItems: List<String>) {
        val voiceNameAdapter = ArrayAdapter(this, R.layout.item, voiceNameItems)
        (voiceNameField.editText as? AutoCompleteTextView)?.setAdapter(voiceNameAdapter)
        (voiceNameField.editText as? AutoCompleteTextView)?.setText("")
    }
}