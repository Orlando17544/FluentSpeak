package com.example.android.fluentspeak

import android.app.Application
import com.example.android.fluentspeak.network.ApisRepository

class FluentSpeakApplication: Application() {
    val apisRepository: ApisRepository
        get() = ServiceLocator.provideApisRepository()
}