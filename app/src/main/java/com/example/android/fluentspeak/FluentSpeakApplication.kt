package com.example.android.fluentspeak

import android.app.Application
import android.content.Context
import com.example.android.fluentspeak.network.ApisRepository
import leakcanary.AppWatcher

class FluentSpeakApplication: Application() {

    val apisRepository: ApisRepository
        get() = ServiceLocator.provideApisRepository()
}