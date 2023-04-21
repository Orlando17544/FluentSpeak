package com.example.android.fluentspeak

import android.app.Application
import android.content.Context
import com.example.android.fluentspeak.network.ApisRepository
import leakcanary.AppWatcher

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