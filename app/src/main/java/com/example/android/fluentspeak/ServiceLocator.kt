package com.example.android.fluentspeak

import androidx.annotation.VisibleForTesting
import com.example.android.fluentspeak.network.ApisRepository
import com.example.android.fluentspeak.network.GoogleCloudApi
import com.example.android.fluentspeak.network.OpenAIApi

object ServiceLocator {
    @Volatile
    var apisRepository: ApisRepository? = null
        @VisibleForTesting set

    fun provideApisRepository(): ApisRepository {
        synchronized(this) {
            return apisRepository ?: createApisRepository()
        }
    }

    private fun createApisRepository(): ApisRepository {
        val newRepository = ApisRepository(OpenAIApi, GoogleCloudApi)
        apisRepository = newRepository
        return newRepository
    }
}