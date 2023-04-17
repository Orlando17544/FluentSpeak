package com.example.android.fluentspeak

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.android.fluentspeak.network.ApisRepository

class FluentSpeakApplication: Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: FluentSpeakApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    val apisRepository: ApisRepository
        get() = ServiceLocator.provideApisRepository()
}