package com.example.android.fluentspeak

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences

    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val isFirstStart = sharedPref.getBoolean(getString(R.string.first_start_key), resources.getBoolean(R.bool.first_start_default_value))

        if(isFirstStart) {
            saveDefaultValuesToSharedPreferences()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}