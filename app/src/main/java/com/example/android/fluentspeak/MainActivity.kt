package com.example.android.fluentspeak

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val isFirstStart = sharedPref.getBoolean(getString(R.string.first_start_key), resources.getBoolean(R.bool.first_start_default_value))

        if(isFirstStart) {
            saveDefaultValuesToSharedPreferences()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        NavigationUI.setupWithNavController(bottomNavigation, navController)
    }

    fun saveDefaultValuesToSharedPreferences() {
        with (sharedPref.edit()) {
            putFloat(getString(R.string.whisper_temperature_key), resources.getFraction(R.fraction.whisper_temperature_default_value, 1, 1))

            putFloat(getString(R.string.chat_gpt_temperature_key), resources.getFraction(R.fraction.chat_gpt_temperature_default_value, 1, 1))
            putInt(getString(R.string.chat_gpt_max_tokens_key), resources.getInteger(R.integer.chat_gpt_max_tokens_default_value))
            putFloat(getString(R.string.chat_gpt_presence_penalty_key), resources.getFraction(R.fraction.chat_gpt_presence_penalty_default_value, 1, 1))
            putFloat(getString(R.string.chat_gpt_frecuency_penalty_key), resources.getFraction(R.fraction.chat_gpt_frecuency_penalty_default_value, 1, 1))

            putString(getString(R.string.text_to_speech_accent_key), resources.getString(R.string.text_to_speech_accent_default_value))
            putString(getString(R.string.text_to_speech_gender_key), resources.getString(R.string.text_to_speech_gender_default_value))
            putString(getString(R.string.text_to_speech_voice_name_key), resources.getString(R.string.text_to_speech_voice_name_default_value))

            putBoolean(getString(R.string.first_start_key), false)

            apply()
        }
    }
}