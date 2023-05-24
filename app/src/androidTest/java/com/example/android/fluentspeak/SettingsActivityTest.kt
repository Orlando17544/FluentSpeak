package com.example.android.fluentspeak

import android.content.Context
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.fluentspeak.network.Message
import com.google.android.material.slider.Slider
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    /*

    private val MILLISECONDS = 4000L

    fun setValue(value: Float): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String {
                return "Set Slider value to $value"
            }

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(Slider::class.java)
            }

            override fun perform(uiController: UiController?, view: View) {
                val seekBar = view as Slider
                seekBar.value = value
            }
        }
    }

    @Test
    fun saveOnClickListener_eightMaxValuesChanged_eightValuesSavedToSharedPreferences() {
        // Given
        launchActivity<SettingsActivity>()

        // When
        onView(withId(R.id.whisper_temperature_slider)).perform(setValue(1f))

        onView(withId(R.id.chat_gpt_temperature_slider)).perform(setValue(2f))
        onView(withId(R.id.chat_gpt_max_tokens_edit)).perform(replaceText("500"))
        onView(withId(R.id.chat_gpt_presence_penalty_slider)).perform(setValue(2f))
        onView(withId(R.id.chat_gpt_frecuency_penalty_slider)).perform(setValue(2f))

        onView(withId(R.id.text_to_speech_accent_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("en-AU")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.text_to_speech_gender_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("MALE")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.text_to_speech_voice_name_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("en-AU-Neural2-B")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.save_button)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)

        // Then
        val applicationContext = ApplicationProvider.getApplicationContext<FluentSpeakApplication>()
        val sharedPref = applicationContext.getSharedPreferences(applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val whisperTemperature = sharedPref.getFloat(applicationContext.getString(R.string.whisper_temperature_key), 0.0f)

        val chatGptTemperature = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_temperature_key), 0.0f)
        val chatGptMaxTokens = sharedPref.getInt(applicationContext.getString(R.string.chat_gpt_max_tokens_key), 0)
        val chatGptPresence = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_presence_penalty_key), 0.0f)
        val chatGptFrecuency = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f)

        val textToSpeechAccent = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_accent_key), "")
        val textToSpeechGender = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_gender_key), "")
        val textToSpeechVoice = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_voice_name_key), "")

        Assert.assertEquals(whisperTemperature, 1f)

        Assert.assertEquals(chatGptTemperature, 2f)
        Assert.assertEquals(chatGptMaxTokens.toString(), "500")
        Assert.assertEquals(chatGptPresence, 2f)
        Assert.assertEquals(chatGptFrecuency, 2f)

        Assert.assertEquals(textToSpeechAccent, "en-AU")
        Assert.assertEquals(textToSpeechGender, "MALE")
        Assert.assertEquals(textToSpeechVoice, "en-AU-Neural2-B")
    }

    @Test
    fun saveOnClickListener_eightMinValuesChanged_eightValuesSavedToSharedPreferences() {
        // Given
        launchActivity<SettingsActivity>()

        // When
        onView(withId(R.id.whisper_temperature_slider)).perform(setValue(0f))

        onView(withId(R.id.chat_gpt_temperature_slider)).perform(setValue(0f))
        onView(withId(R.id.chat_gpt_max_tokens_edit)).perform(replaceText("001000"))
        onView(withId(R.id.chat_gpt_presence_penalty_slider)).perform(setValue(-2f))
        onView(withId(R.id.chat_gpt_frecuency_penalty_slider)).perform(setValue(-2f))

        onView(withId(R.id.text_to_speech_accent_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("en-GB")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.text_to_speech_gender_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("FEMALE")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.text_to_speech_voice_name_field)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)
        onView(withText("en-GB-Neural2-F")).inRoot(isPlatformPopup()).perform(click())
        Thread.sleep(MILLISECONDS)

        onView(withId(R.id.save_button)).perform(scrollTo(), click())
        Thread.sleep(MILLISECONDS)

        // Then
        val applicationContext = ApplicationProvider.getApplicationContext<FluentSpeakApplication>()
        val sharedPref = applicationContext.getSharedPreferences(applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val whisperTemperature = sharedPref.getFloat(applicationContext.getString(R.string.whisper_temperature_key), 0.0f)

        val chatGptTemperature = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_temperature_key), 0.0f)
        val chatGptMaxTokens = sharedPref.getInt(applicationContext.getString(R.string.chat_gpt_max_tokens_key), 0)
        val chatGptPresence = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_presence_penalty_key), 0.0f)
        val chatGptFrecuency = sharedPref.getFloat(applicationContext.getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f)

        val textToSpeechAccent = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_accent_key), "")
        val textToSpeechGender = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_gender_key), "")
        val textToSpeechVoice = sharedPref.getString(applicationContext.getString(R.string.text_to_speech_voice_name_key), "")

        Assert.assertEquals(whisperTemperature, 0f)

        Assert.assertEquals(chatGptTemperature, 0f)
        Assert.assertEquals(chatGptMaxTokens.toString(), "1000")
        Assert.assertEquals(chatGptPresence, -2f)
        Assert.assertEquals(chatGptFrecuency, -2f)

        Assert.assertEquals(textToSpeechAccent, "en-GB")
        Assert.assertEquals(textToSpeechGender, "FEMALE")
        Assert.assertEquals(textToSpeechVoice, "en-GB-Neural2-F")
    }*/
}